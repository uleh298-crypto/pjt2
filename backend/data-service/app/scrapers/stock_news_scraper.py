"""네이버 증권 종목뉴스 스크래퍼

네이버 증권에서 종목별 뉴스를 크롤링합니다.
- 네이버가 이미 뉴스-종목 매핑을 해놓음
- 100% 본문 추출 가능 (n.news.naver.com)
- LLM 분석 불필요
"""
import re
import random
import logging
import asyncio
from datetime import datetime
from typing import Optional, List, Dict, Any
from dataclasses import dataclass

import httpx
from bs4 import BeautifulSoup
from sqlalchemy.orm import Session

from app.models.news import NewsArticle
from app.models.news_stock import NewsStockMapping
from app.models.company import CompanyInfo
from app.models.stock import Stock

logger = logging.getLogger(__name__)


@dataclass
class ScrapedNews:
    """크롤링된 뉴스 데이터"""
    title: str
    content: str
    source: str
    source_url: str
    thumbnail_url: Optional[str]
    published_at: Optional[datetime]


class StockNewsScraper:
    """
    네이버 증권 종목뉴스 스크래퍼

    Flow:
    1. 종목 메인 페이지에서 뉴스 링크 추출
    2. news_read.naver → redirect URL 추출
    3. n.news.naver.com에서 본문 추출
    """

    BASE_URL = "https://finance.naver.com"

    # User-Agent 로테이션 (429 Rate Limit 방지)
    USER_AGENTS = [
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    ]

    HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    }

    def __init__(self, db: Session):
        self.db = db
        self.client: Optional[httpx.AsyncClient] = None

    async def __aenter__(self):
        self.client = httpx.AsyncClient(headers=self.HEADERS, timeout=30.0)
        return self

    async def __aexit__(self, *args):
        if self.client:
            await self.client.aclose()

    async def _get_with_retry(self, url: str, max_retries: int = 3) -> Optional[str]:
        """재시도 로직이 포함된 GET 요청 (429 Rate Limit 대응)"""
        for attempt in range(max_retries):
            try:
                # 요청 간 랜덤 대기 (2~4초로 증가)
                await asyncio.sleep(random.uniform(2.0, 4.0))

                # 매 요청마다 User-Agent 변경
                self.client.headers["User-Agent"] = random.choice(self.USER_AGENTS)

                response = await self.client.get(url)
                response.raise_for_status()
                return response.text
            except httpx.HTTPStatusError as e:
                # 429 Too Many Requests 특별 처리
                if e.response.status_code == 429:
                    wait_time = 30 + (attempt * 15)  # 30초, 45초, 60초
                    logger.warning(f"429 Rate Limit - {wait_time}초 대기 후 재시도: {url}")
                    await asyncio.sleep(wait_time)
                else:
                    logger.warning(f"요청 실패 (시도 {attempt + 1}/{max_retries}): {url} - {e}")
                    if attempt < max_retries - 1:
                        await asyncio.sleep(3 * (attempt + 1))
            except Exception as e:
                logger.warning(f"요청 실패 (시도 {attempt + 1}/{max_retries}): {url} - {e}")
                if attempt < max_retries - 1:
                    await asyncio.sleep(3 * (attempt + 1))
        return None

    async def get_news_links(self, stock_code: str) -> List[str]:
        """종목 메인 페이지에서 뉴스 링크 추출"""
        url = f"{self.BASE_URL}/item/main.naver?code={stock_code}"
        html = await self._get_with_retry(url)

        if not html:
            return []

        soup = BeautifulSoup(html, "lxml")
        links = []

        # news_read.naver 링크 추출
        for a_tag in soup.select("a[href*='news_read.naver']"):
            href = a_tag.get("href", "")
            if href and href not in links:
                full_url = self.BASE_URL + href if href.startswith("/") else href
                links.append(full_url)

        return links

    async def extract_redirect_url(self, news_read_url: str) -> Optional[str]:
        """news_read.naver에서 실제 뉴스 URL 추출"""
        html = await self._get_with_retry(news_read_url)

        if not html:
            return None

        # script에서 redirect URL 추출
        match = re.search(r"top\.location\.href='([^']+)'", html)
        if match:
            return match.group(1)

        return None

    async def scrape_article(self, news_url: str) -> Optional[ScrapedNews]:
        """n.news.naver.com에서 기사 본문 추출"""
        html = await self._get_with_retry(news_url)

        if not html:
            return None

        soup = BeautifulSoup(html, "lxml")

        # 제목
        title_el = soup.select_one("h2#title_area span") or soup.select_one("h2.media_end_head_headline")
        title = title_el.get_text(strip=True) if title_el else ""

        if not title:
            logger.warning(f"제목 추출 실패: {news_url}")
            return None

        # 본문 (문단 구분 유지)
        content_el = soup.select_one("#dic_area") or soup.select_one("#newsct_article")
        content = ""
        if content_el:
            # <br> 태그를 줄바꿈으로 변환
            for br in content_el.find_all("br"):
                br.replace_with("\n")
            # 텍스트 추출 후 문단 정리
            raw_text = content_el.get_text(separator="\n")
            # 연속 줄바꿈 정리 (3개 이상 → 2개)
            lines = [line.strip() for line in raw_text.split("\n")]
            content = "\n\n".join(line for line in lines if line)

        # 언론사
        source_el = soup.select_one("a.media_end_head_top_logo img")
        source = source_el.get("alt", "") if source_el else ""
        if not source:
            source_el = soup.select_one(".media_end_head_top_logo_text")
            source = source_el.get_text(strip=True) if source_el else "네이버뉴스"

        # 썸네일
        thumb_el = soup.select_one("meta[property='og:image']")
        thumbnail_url = thumb_el.get("content") if thumb_el else None

        # 발행일
        time_el = soup.select_one("span.media_end_head_info_datestamp_time")
        published_at = None
        if time_el:
            date_str = time_el.get("data-date-time") or time_el.get_text(strip=True)
            try:
                published_at = datetime.fromisoformat(date_str.replace(".", "-").replace(" ", "T")[:19])
            except:
                pass

        return ScrapedNews(
            title=title,
            content=content,
            source=source,
            source_url=news_url,
            thumbnail_url=thumbnail_url,
            published_at=published_at
        )

    def _get_category_from_industry(self, industry_group: Optional[str]) -> str:
        """
        산업 그룹(21개 group_code)에서 뉴스 카테고리(14개) 추출

        매핑 규칙 (산업분류_체계_설계.md 섹션 3.2):
        - 반도체: IT_SEMI
        - IT/전자: IT_ELEC, IT_SW
        - 바이오/의약: BIO
        - 자동차: AUTO
        - 화학/소재: CHEM, STEEL
        - 에너지: ENERGY
        - 금융: FINANCE, INSURANCE
        - 건설/부동산: CONSTRUCT
        - 소비재: CONSUMER, RETAIL, FOOD
        - 통신/미디어: TELECOM
        - 운송/물류: TRANSPORT, SHIPBUILD
        - 산업재: MACHINERY, DEFENSE
        - 기타: HOLDING, AGRI, MINING, EVENT, ETC, OTHER
        """
        mapping = {
            # 반도체
            "IT_SEMI": "NEWS_SEMI",
            # IT/전자 (통합)
            "IT_ELEC": "NEWS_IT",
            "IT_SW": "NEWS_IT",
            # 바이오/의약
            "BIO": "NEWS_BIO",
            # 자동차
            "AUTO": "NEWS_AUTO",
            # 화학/소재 (통합)
            "CHEM": "NEWS_CHEM",
            "STEEL": "NEWS_CHEM",
            # 에너지
            "ENERGY": "NEWS_ENERGY",
            # 금융 (통합)
            "FINANCE": "NEWS_FINANCE",
            "INSURANCE": "NEWS_FINANCE",
            # 건설/부동산
            "CONSTRUCT": "NEWS_CONSTRUCT",
            # 소비재 (통합)
            "CONSUMER": "NEWS_CONSUMER",
            "RETAIL": "NEWS_CONSUMER",
            "FOOD": "NEWS_CONSUMER",
            # 통신/미디어
            "TELECOM": "NEWS_TELECOM",
            # 운송/물류 (통합)
            "TRANSPORT": "NEWS_TRANSPORT",
            "SHIPBUILD": "NEWS_TRANSPORT",
            # 산업재 (통합)
            "MACHINERY": "NEWS_INDUSTRY",
            "DEFENSE": "NEWS_INDUSTRY",
            # 기타 (통합)
            "HOLDING": "NEWS_ETC",
            "AGRI": "NEWS_ETC",
            "MINING": "NEWS_ETC",
            "EVENT": "NEWS_ETC",
            "ETC": "NEWS_ETC",
            "OTHER": "NEWS_ETC",
        }
        return mapping.get(industry_group, "NEWS_ETC")

    async def scrape_stock_news(
        self,
        stock_code: str,
        max_articles: int = 10
    ) -> Dict[str, int]:
        """
        특정 종목의 뉴스 크롤링 및 저장

        Args:
            stock_code: 종목 코드 (6자리)
            max_articles: 최대 기사 수

        Returns:
            {"total": int, "new": int, "mapped": int}
        """
        # 주식 정보 조회 (ticker로 검색)
        stock = self.db.query(Stock).filter(
            Stock.ticker == stock_code,
            Stock.is_active == True
        ).first()

        if not stock:
            logger.warning(f"주식 정보 없음: {stock_code}")
            return {"total": 0, "new": 0, "mapped": 0}

        # 회사 정보 조회 (카테고리용)
        company = self.db.query(CompanyInfo).filter(
            CompanyInfo.id == stock.company_id
        ).first() if stock.company_id else None

        category = self._get_category_from_industry(company.industry_group if company else None)
        stats = {"total": 0, "new": 0, "mapped": 0}

        # 뉴스 링크 추출 (메인 페이지에서)
        news_links = await self.get_news_links(stock_code)

        if not news_links:
            logger.info(f"뉴스 없음: {stock_code}")
            return stats

        for link in news_links[:max_articles]:
            stats["total"] += 1

            # redirect URL 추출
            real_url = await self.extract_redirect_url(link)
            if not real_url:
                continue

            # 중복 체크
            existing = self.db.query(NewsArticle).filter(
                NewsArticle.source_url == real_url
            ).first()

            if existing:
                # 이미 있는 뉴스면 매핑만 추가
                if stock.company_id:
                    self._add_stock_mapping(existing.id, stock.company_id)
                stats["mapped"] += 1
                continue

            # 기사 크롤링
            article = await self.scrape_article(real_url)
            if not article:
                continue

            # DB 저장
            news = NewsArticle(
                title=article.title,
                content=article.content,
                source=article.source,
                source_url=article.source_url,
                thumbnail_url=article.thumbnail_url,
                category_code=category,
                published_at=article.published_at
            )
            self.db.add(news)
            self.db.flush()  # ID 생성

            # 종목 매핑
            if stock.company_id:
                self._add_stock_mapping(news.id, stock.company_id)

            stats["new"] += 1
            logger.info(f"뉴스 저장: [{stock_code}] {article.title[:30]}...")

        self.db.commit()
        logger.info(f"종목뉴스 크롤링 완료: {stock_code} - 총 {stats['total']}건, 신규 {stats['new']}건")

        return stats

    def _add_stock_mapping(self, news_id: int, company_id: int):
        """뉴스-종목 매핑 추가 (중복 무시)"""
        existing = self.db.query(NewsStockMapping).filter(
            NewsStockMapping.news_id == news_id,
            NewsStockMapping.company_id == company_id
        ).first()

        if not existing:
            mapping = NewsStockMapping(
                news_id=news_id,
                company_id=company_id
            )
            self.db.add(mapping)

# 스케줄러/API용 함수
async def scrape_stock_news(db: Session, stock_code: str, max_articles: int = 10) -> Dict[str, int]:
    """단일 종목 뉴스 크롤링"""
    async with StockNewsScraper(db) as scraper:
        return await scraper.scrape_stock_news(stock_code, max_articles=max_articles)
