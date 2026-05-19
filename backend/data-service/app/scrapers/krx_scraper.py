"""KRX KIND 공시 크롤러 - ETF 상장폐지/정리매매/투자유의 모니터링"""
import httpx
import logging
import re
from datetime import datetime, date, timedelta
from typing import List, Optional, Dict
from bs4 import BeautifulSoup
from sqlalchemy.orm import Session
from sqlalchemy import and_

from app.models.etf_disclosure import EtfDisclosure, DisclosureType
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class KrxDisclosureScraper:
    """
    KRX KIND 공시 크롤러

    수집 대상:
    - 상장폐지 결정
    - 정리매매 지정
    - 투자유의/투자경고 지정

    스케줄: 매일 09:00 (1회)
    """

    # KRX KIND API 엔드포인트
    KIND_API_URL = "https://kind.krx.co.kr/disclosure/searchtotalinfo.do"

    USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    TIMEOUT = 30

    # ETF 관련 공시 키워드
    DISCLOSURE_KEYWORDS = {
        DisclosureType.DELISTING: ["상장폐지", "상장적격성", "폐지결정"],
        DisclosureType.LIQUIDATION: ["정리매매", "매매거래정지"],
        DisclosureType.CAUTION: ["투자유의", "투자주의"],
        DisclosureType.SURVEILLANCE: ["투자경고", "투자위험"],
    }

    def __init__(self, db: Session):
        self.db = db
        self.client = httpx.AsyncClient(
            headers={"User-Agent": self.USER_AGENT},
            timeout=self.TIMEOUT,
            follow_redirects=True
        )

    async def close(self):
        await self.client.aclose()

    async def scrape_disclosures(self, days_back: int = 7) -> Dict[str, int]:
        """
        최근 N일간 ETF 관련 공시 수집

        Args:
            days_back: 몇 일 전까지 조회할지 (기본 7일)

        Returns:
            {"total": 수집건수, "new": 신규건수}
        """
        result = {"total": 0, "new": 0}

        # 검색 기간 설정
        end_date = date.today()
        start_date = end_date - timedelta(days=days_back)

        logger.info(f"KRX KIND 공시 수집 시작: {start_date} ~ {end_date}")

        try:
            # ETF 관련 공시 검색
            disclosures = await self._search_etf_disclosures(start_date, end_date)
            result["total"] = len(disclosures)

            # 신규 공시만 저장
            for disclosure_data in disclosures:
                if self._save_if_new(disclosure_data):
                    result["new"] += 1

            logger.info(
                f"KRX KIND 공시 수집 완료: "
                f"총 {result['total']}건, 신규 {result['new']}건"
            )

        except Exception as e:
            logger.error(f"KRX KIND 공시 수집 실패: {e}")

        return result

    async def _search_etf_disclosures(
        self, start_date: date, end_date: date
    ) -> List[Dict]:
        """ETF 관련 공시 검색"""
        disclosures = []

        # 각 키워드로 검색
        search_keywords = ["ETF 상장폐지", "ETF 정리매매", "ETF 투자유의", "ETF 투자경고"]

        for keyword in search_keywords:
            try:
                results = await self._search_kind(keyword, start_date, end_date)
                disclosures.extend(results)
            except Exception as e:
                logger.error(f"검색 실패 [{keyword}]: {e}")
                continue

        # 중복 제거 (disclosure_title + etf_code 기준)
        seen = set()
        unique_disclosures = []
        for d in disclosures:
            key = (d.get("etf_code"), d.get("disclosure_title"))
            if key not in seen:
                seen.add(key)
                unique_disclosures.append(d)

        return unique_disclosures

    async def _search_kind(
        self, keyword: str, start_date: date, end_date: date
    ) -> List[Dict]:
        """KIND 검색 API 호출"""
        results = []

        # KIND 검색 파라미터
        params = {
            "method": "searchTotalInfoSub",
            "forward": "searchtotalinfo_detail",
            "searchCodeType": "",
            "searchCorpName": keyword,
            "repIsuSrtCd": "",
            "fdName": "all_mktact_idx",
            "pageIndex": "1",
            "currentPageSize": "100",
            "orderMode": "1",
            "orderStat": "D",
            "fromData": start_date.strftime("%Y%m%d"),
            "toData": end_date.strftime("%Y%m%d"),
        }

        try:
            response = await self.client.get(self.KIND_API_URL, params=params)
            response.raise_for_status()

            # HTML 파싱
            soup = BeautifulSoup(response.text, "html.parser")
            rows = soup.select("table tbody tr")

            for row in rows:
                try:
                    disclosure = self._parse_disclosure_row(row)
                    if disclosure and self._is_etf_related(disclosure):
                        results.append(disclosure)
                except Exception as e:
                    logger.debug(f"행 파싱 실패: {e}")
                    continue

        except httpx.HTTPError as e:
            logger.error(f"KIND API 요청 실패: {e}")

        return results

    def _parse_disclosure_row(self, row) -> Optional[Dict]:
        """공시 테이블 행 파싱"""
        cols = row.find_all("td")
        if len(cols) < 4:
            return None

        try:
            # 종목코드/종목명
            corp_info = cols[0].get_text(strip=True)
            etf_code = self._extract_code(corp_info)
            etf_name = self._extract_name(corp_info)

            # 공시 제목
            title_tag = cols[1].find("a")
            disclosure_title = title_tag.get_text(strip=True) if title_tag else cols[1].get_text(strip=True)

            # 공시 URL
            source_url = None
            if title_tag and title_tag.get("href"):
                source_url = "https://kind.krx.co.kr" + title_tag.get("href")

            # 공시일
            date_str = cols[2].get_text(strip=True)
            disclosure_date = self._parse_date(date_str)

            # 공시 유형 분류
            disclosure_type = self._classify_disclosure(disclosure_title)

            return {
                "etf_code": etf_code,
                "etf_name": etf_name,
                "disclosure_title": disclosure_title,
                "disclosure_type": disclosure_type,
                "disclosure_date": disclosure_date,
                "source_url": source_url,
            }

        except Exception as e:
            logger.debug(f"파싱 오류: {e}")
            return None

    def _extract_code(self, text: str) -> str:
        """종목코드 추출 (6자리 숫자)"""
        match = re.search(r'\d{6}', text)
        return match.group() if match else ""

    def _extract_name(self, text: str) -> str:
        """종목명 추출"""
        # 숫자 코드 제거
        name = re.sub(r'\d{6}', '', text)
        # 괄호 및 특수문자 정리
        name = re.sub(r'[\[\]\(\)]', '', name)
        return name.strip()

    def _parse_date(self, date_str: str) -> date:
        """날짜 문자열 파싱"""
        date_str = date_str.strip()
        for fmt in ["%Y.%m.%d", "%Y-%m-%d", "%Y/%m/%d", "%Y%m%d"]:
            try:
                return datetime.strptime(date_str, fmt).date()
            except ValueError:
                continue
        return date.today()

    def _classify_disclosure(self, title: str) -> str:
        """공시 제목으로 유형 분류"""
        title_lower = title.lower()

        for dtype, keywords in self.DISCLOSURE_KEYWORDS.items():
            for keyword in keywords:
                if keyword in title:
                    return dtype.value

        return DisclosureType.OTHER.value

    def _is_etf_related(self, disclosure: Dict) -> bool:
        """ETF 관련 공시인지 확인"""
        etf_name = disclosure.get("etf_name", "").upper()
        title = disclosure.get("disclosure_title", "").upper()

        # ETF 키워드 포함 여부
        etf_indicators = ["ETF", "상장지수", "인덱스펀드"]
        for indicator in etf_indicators:
            if indicator in etf_name or indicator in title:
                return True

        return False

    def _save_if_new(self, disclosure_data: Dict) -> bool:
        """신규 공시인 경우에만 저장"""
        # 중복 체크 (종목코드 + 공시제목 + 공시일)
        existing = self.db.query(EtfDisclosure).filter(
            and_(
                EtfDisclosure.etf_code == disclosure_data["etf_code"],
                EtfDisclosure.disclosure_title == disclosure_data["disclosure_title"],
                EtfDisclosure.disclosure_date == disclosure_data["disclosure_date"]
            )
        ).first()

        if existing:
            return False

        # 신규 저장
        disclosure = EtfDisclosure(
            etf_code=disclosure_data["etf_code"],
            etf_name=disclosure_data["etf_name"],
            disclosure_type=disclosure_data["disclosure_type"],
            disclosure_title=disclosure_data["disclosure_title"],
            disclosure_date=disclosure_data["disclosure_date"],
            source_url=disclosure_data.get("source_url"),
            is_notified="N"
        )

        try:
            self.db.add(disclosure)
            self.db.commit()
            logger.info(
                f"신규 공시 저장: [{disclosure_data['etf_code']}] "
                f"{disclosure_data['disclosure_title']}"
            )
            return True
        except Exception as e:
            logger.error(f"공시 저장 실패: {e}")
            self.db.rollback()
            return False

    async def get_pending_notifications(self) -> List[EtfDisclosure]:
        """
        알림 미발송 공시 조회 (사용자 포트폴리오 매칭용)

        Returns:
            알림 발송이 필요한 공시 목록
        """
        return self.db.query(EtfDisclosure).filter(
            EtfDisclosure.is_notified == "N"
        ).all()

    def mark_as_notified(self, disclosure_id: int) -> bool:
        """알림 발송 완료 처리"""
        try:
            disclosure = self.db.query(EtfDisclosure).filter(
                EtfDisclosure.disclosure_id == disclosure_id
            ).first()

            if disclosure:
                disclosure.is_notified = "Y"
                self.db.commit()
                return True
            return False
        except Exception as e:
            logger.error(f"알림 상태 업데이트 실패: {e}")
            self.db.rollback()
            return False


# 스케줄러에서 호출할 함수
async def scheduled_krx_disclosure_check(db: Session) -> Dict[str, int]:
    """스케줄러용 KRX 공시 체크 함수"""
    scraper = KrxDisclosureScraper(db)
    try:
        return await scraper.scrape_disclosures(days_back=7)
    finally:
        await scraper.close()
