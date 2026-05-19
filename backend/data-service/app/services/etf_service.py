import asyncio
import logging

from sqlalchemy.ext.asyncio import AsyncSession

from app.config import get_settings
from app.repositories.etf_repository import EtfRepository, EtfPriceRepository
from app.scrapers.dependencies import get_pykrx_client

from app.repositories.stock_repository import StockRepository

logger = logging.getLogger(__name__)
settings = get_settings()

from fastapi import BackgroundTasks
from app.database import AsyncSessionLocal
import traceback

_krx_api_semaphore = asyncio.Semaphore(5)

# KIS API 섹터 문자열 + ETF 이름 → Java EtfSector enum 값 변환
# EtfSector: SEMI, IT, BIO, AUTO, CHEM, ENERGY, FINANCE, CONSTRUCT,
#            CONSUMER, TELECOM, TRANSPORT, INDUSTRY, HOLDING, ETC
_SECTOR_KEYWORD_MAP = [
    ("SEMI",      ["반도체"]),
    ("IT",        ["소프트웨어", "it", "전자", "정보기술", "인터넷", "게임", "ai", "인공지능", "클라우드"]),
    ("BIO",       ["바이오", "의약", "헬스", "의료", "헬스케어", "제약"]),
    ("AUTO",      ["자동차", "2차전지", "전기차", "배터리"]),
    ("CHEM",      ["화학", "소재", "철강", "비철"]),
    ("ENERGY",    ["에너지", "원유", "천연가스", "신재생", "태양광", "풍력"]),
    ("FINANCE",   ["금융", "은행", "보험", "증권", "자산운용"]),
    ("CONSTRUCT", ["건설", "부동산", "리츠", "reit"]),
    ("CONSUMER",  ["소비재", "유통", "식품", "음식", "경기소비재", "필수소비재"]),
    ("TELECOM",   ["통신", "미디어", "방송", "커뮤니케이션"]),
    ("TRANSPORT", ["운송", "물류", "항공", "해운", "조선"]),
    ("INDUSTRY",  ["산업재", "기계", "방산", "방위"]),
    ("HOLDING",   ["지주"]),
]

def _normalize_etf_sector(kis_sector: str, etf_name: str = "") -> str:
    """KIS API 섹터 문자열 + ETF 이름을 Java EtfSector enum 값으로 변환"""
    combined = f"{kis_sector} {etf_name}".lower()
    for sector_code, keywords in _SECTOR_KEYWORD_MAP:
        if any(kw in combined for kw in keywords):
            return sector_code
    return "ETC"


class EtfService:
    def __init__(self, db: AsyncSession):
        self.etf_repository = EtfRepository(db)
        self.etf_price_repository = EtfPriceRepository(db)
        self.stock_repository = StockRepository(db)
        self.pykrx_client = get_pykrx_client()

    async def sync_etf_tickers(self, background_tasks: BackgroundTasks = None):
        # 오늘 자 기준 상장된 etf ticker 리스트
        listed_etfs = await self.pykrx_client.get_today_etf_list()

        # KODEX, TIGER만 필터링
        filtered_etfs = [etf for etf in listed_etfs if etf.etf_manager in ("KODEX", "TIGER")]
        logger.info(f"KODEX/TIGER 필터링: {len(listed_etfs)}개 → {len(filtered_etfs)}개")

        # db 에 있는 etf tickers
        etf_tickers_in_db = set(await self.etf_repository.get_etf_tickers())
        etfs = []
        for listed_etf in filtered_etfs:
            if listed_etf.ticker in etf_tickers_in_db:
                logger.debug(f"[{listed_etf.ticker}] 이미 DB에 존재함")
                continue
            etfs.append(listed_etf)

        if not etfs:
            logger.info("신규 ETF가 없습니다.")
            return

        infos = await self.etf_repository.save_initial_etf_infos(etfs)
        logger.info(f"{len(infos)}개의 신규 ETF가 기본 정보 수집 완료되었습니다.")

    async def update_etfs_active_status(self):
        unchecked_etfs = await self.etf_repository.get_unchecked_etfs()
        if not unchecked_etfs:
            logger.info("상태 검사가 필요한 신규 ETF가 없습니다.")
            return

        foreign_keywords = [
            '미국', '중국', '일본', '유로', '유럽', '인도', '베트남', '글로벌',
            '차이나', '항셍', '러셀', '나스닥', 'S&P', '달러', '선진', '신흥국',
            '대만', '프랑스', '독일', '영국', 'MSCI', '브라질', '멕시코', '라틴',
            '아시아', '한중', '필라델피아', 'STOXX', 'CSI', '니케이', 'TOPIX',
            'STAR50', '엔화', '위안화', '월드', '테슬라', '엔비디아', '애플', '이머징', '(H)'
        ]

        async def check_and_update_etf(etf: dict):
            ticker = etf["ticker"]
            etf_id = etf["id"]
            etf_name = etf.get("name", "")

            if any(k in etf_name.upper() for k in foreign_keywords):
                logger.info(f"[{ticker}] 이름({etf_name}) 기반 해외 자산 판별 (is_krx_only=False)")
                await self.etf_repository.update_krx_status(etf_id, False)
                return

            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                pdf_infos = await self.pykrx_client.get_etf_pdf_info(ticker)

                if not pdf_infos:
                    logger.warning(f"[{ticker}] PDF 구성종목을 조회하지 못했습니다.")
                    return

                has_foreign_stock = False
                import re
                for pdf in pdf_infos:
                    pdf_ticker = pdf["ticker"].strip()
                    pdf_name = pdf["name"].strip()

                    is_standard_stock = pdf_ticker.isdigit() and len(pdf_ticker) == 6
                    has_korean_name = bool(re.search(r'[가-힣]', pdf_name))
                    is_krw = pdf_ticker.upper() == 'KRW'

                    if not (is_standard_stock or has_korean_name or is_krw):
                        has_foreign_stock = True
                        break

                is_krx_only = not has_foreign_stock
                logger.info(f"[{ticker}] 국내 전용 여부 판별 완료 (is_krx_only={is_krx_only})")
                await self.etf_repository.update_krx_status(etf_id, is_krx_only)

        # 모든 ETF 병렬 처리
        await asyncio.gather(*[check_and_update_etf(etf) for etf in unchecked_etfs], return_exceptions=True)

    async def force_update_all_etfs_active_status(self):
        """기존 DB의 모든 ETF를 대상으로 PDF를 재검사하여 is_krx_only 상태를 강제 업데이트합니다."""
        all_etfs = await self.etf_repository.get_all_etfs()
        if not all_etfs:
            logger.info("상태 검사가 필요한 ETF가 없습니다.")
            return

        foreign_keywords = [
            '미국', '중국', '일본', '유로', '유럽', '인도', '베트남', '글로벌',
            '차이나', '항셍', '러셀', '나스닥', 'S&P', '달러', '선진', '신흥국',
            '대만', '프랑스', '독일', '영국', 'MSCI', '브라질', '멕시코', '라틴',
            '아시아', '한중', '필라델피아', 'STOXX', 'CSI', '니케이', 'TOPIX',
            'STAR50', '엔화', '위안화', '월드', '테슬라', '엔비디아', '애플', '이머징', '(H)'
        ]

        async def force_check_and_update_etf(etf: dict):
            ticker = etf["ticker"]
            etf_id = etf["id"]
            etf_name = etf.get("name", "")

            if any(k in etf_name.upper() for k in foreign_keywords):
                logger.info(f"[{ticker}] 이름({etf_name}) 기반 해외 자산 판별 강제 업데이트 (is_krx_only=False)")
                await self.etf_repository.update_krx_status(etf_id, False)
                return

            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                pdf_infos = await self.pykrx_client.get_etf_pdf_info(ticker)

                if not pdf_infos:
                    logger.warning(f"[{ticker}] PDF 구성종목을 조회하지 못했습니다.")
                    return

                has_foreign_stock = False
                import re
                for pdf in pdf_infos:
                    pdf_ticker = pdf["ticker"].strip()
                    pdf_name = pdf["name"].strip()

                    is_standard_stock = pdf_ticker.isdigit() and len(pdf_ticker) == 6
                    has_korean_name = bool(re.search(r'[가-힣]', pdf_name))
                    is_krw = pdf_ticker.upper() == 'KRW'

                    if not (is_standard_stock or has_korean_name or is_krw):
                        has_foreign_stock = True
                        break

                is_krx_only = not has_foreign_stock
                logger.info(f"[{ticker}] 강제 국내 전용 여부 판별 업데이트 완료 (is_krx_only={is_krx_only})")
                await self.etf_repository.update_krx_status(etf_id, is_krx_only)

        # 모든 ETF 병렬 처리
        await asyncio.gather(*[force_check_and_update_etf(etf) for etf in all_etfs], return_exceptions=True)

    async def sync_etf_prices(self):
        from datetime import date, datetime, timedelta
        # 활성화된(국내 전용) ETF들만 가격 이력을 수집합니다
        active_etfs = await self.etf_repository.get_active_etfs()
        if not active_etfs:
            logger.info("가격 이력을 수집할 활성 ETF가 없습니다.")
            return

        now = datetime.now()
        # 오후 4시(16:00) 이전이면 어제 데이터를 최신 기준으로 설정
        target_end_date = now.date() if now.hour >= 16 else now.date() - timedelta(days=1)
        
        for etf in active_etfs:
            ticker = etf["ticker"]
            etf_id = etf["id"]
            
            latest_date = await self.etf_price_repository.get_latest_price_date(etf_id)
            if latest_date is None:
                start_date = "20230302"
            else:
                if latest_date >= target_end_date:
                    logger.debug(f"[{ticker}] 이미 최신 날짜({latest_date})의 가격 이력이 존재합니다.")
                    continue
                next_date = latest_date + timedelta(days=1)
                if next_date > target_end_date:
                    continue  # 이미 최신
                
                import pandas as pd
                # 영업일(월~금)이 하루도 포함되어 있지 않으면 불필요한 API 호출 생략 (주말 건너뛰기)
                if len(pd.bdate_range(start=next_date, end=target_end_date)) == 0:
                    continue

                start_date = next_date.strftime("%Y%m%d")
                
            async with _krx_api_semaphore:
                await asyncio.sleep(0.2)
                price_histories = await self.pykrx_client.get_price_history(
                    ticker, 
                    start_date=start_date,
                    end_date=target_end_date.strftime("%Y%m%d")
                )

                if not price_histories:
                    continue

                # DB 직전 종가 기준으로 change_rate 계산
                prev_close = await self.etf_price_repository.get_latest_close(etf_id)
                for history in price_histories:
                    history['etf_id'] = etf_id
                    history['created_at'] = now
                    cur_close = history.get('close') or 0
                    if prev_close and prev_close != 0:
                        history['change_rate'] = round((cur_close - float(prev_close)) / float(prev_close) * 100, 4)
                    else:
                        history['change_rate'] = 0.0
                    prev_close = cur_close  # 다음 행의 기준가로 업데이트

                logging.debug(f"[{ticker}] DB 가격 이력 {len(price_histories)}건 적재 완료.")
                await self.etf_price_repository.save_bulk(price_histories)

    async def update_empty_company_infos(self):
        tickers = await self.stock_repository.get_stocks_with_empty_company_info()
        if not tickers:
            logger.info("회사 정보 동기화가 필요한 국내 주식이 없습니다.")
            return
            
        logger.info(f"회사 정보가 누락된 주식 {len(tickers)}건 수집 시작...")
        await self.process_domestic_stocks(tickers)

    async def process_domestic_stocks(self, tickers: list[str]):
        from app.scrapers.data_portal_client import DataPortalClient
        client = DataPortalClient()
        for idx, ticker in enumerate(tickers, start=1):
            try:
                # 1. ticker로 사업자등록번호(crno) 및 기본 회사명 조회
                item_info = await client.get_stock_item_info(ticker)
                if not item_info:
                    # API로 못찾는 경우라도 주식 테이블엔 저장되도록
                    await self.stock_repository.get_or_create_stock(ticker, None, None)
                else:
                    corp_name = item_info.get("corpNm")
                    market_type = item_info.get("mrktCtg")
                    corp_number = item_info.get("crno")

                    if not corp_name:
                        await self.stock_repository.get_or_create_stock(ticker, None, market_type)
                    else:
                        # 2. Company 저장 (외래키 대상)
                        company = await self.stock_repository.get_or_create_company(corp_name, corp_number)

                        # 3. Stock 저장 (Company 외래키 연결)
                        await self.stock_repository.get_or_create_stock(ticker, company.id, market_type)

                        # 4. 사업자등록번호(crno)로 회사 세부정보 조회 및 채우기
                        if corp_number:
                            corp_outline = await client.get_corp_outline(corp_number)
                            if corp_outline:
                                info = {
                                    "industry_name": corp_outline.get("sicNm"),
                                    "ceo_name": corp_outline.get("enpRprFnm"),
                                    "homepage": corp_outline.get("enpHmpgUrl"),
                                    "region": corp_outline.get("enpBsadr"),
                                    "corporation_number": corp_number
                                }
                                await self.stock_repository.update_company_info(company.id, info)
                                
                                biz_description = corp_outline.get("enpMainBizNm")
                                if biz_description:
                                    await self.stock_repository.update_stock_description(ticker, biz_description)
            except Exception as e:
                logging.error(f"[{ticker}] 처리 중 오류 발생: {e}")
                await self.stock_repository.db.rollback()
                continue

            # 10개마다 중간 commit하여 DB에 즉시 반영
            if idx % 10 == 0:
                await self.stock_repository.db.commit()
                logging.info(f"[진행상황] {idx}/{len(tickers)} 처리 완료, DB commit 완료")

        # 나머지 flush된 데이터 최종 commit
        await self.stock_repository.db.commit()
        logging.info(f"[완료] 전체 {len(tickers)}개 주식 정보 저장 완료")

    async def sync_etf_metadata(self):
        """
        KIS API (FHPST02400000)를 통해 활성 ETF의 상세 메타데이터를 동기화합니다.
        - AUM(순자산총액), NAV, 운용사, 상장일, 배당주기, 대표섹터 등을 채웁니다.
        """
        from app.services.kis_client import KISClient
        from datetime import datetime

        active_etfs = await self.etf_repository.get_active_etfs()
        if not active_etfs:
            logger.info("메타데이터를 동기화할 활성 ETF가 없습니다.")
            return

        kis_client = KISClient()
        logger.info(f"총 {len(active_etfs)}개 ETF 상세 메타데이터 수집 시작...")

        # KIS etf_dvdn_cycl 코드 → dividend_freq 문자열 매핑
        # (069500 기준: etf_dvdn_cycl=3 → QUARTERLY 확인됨)
        dividend_freq_map = {
            "1": "MONTHLY",
            "3": "QUARTERLY",
            "6": "SEMI_AN",
            "12": "ANNUAL",
            "0": "NONE"
        }

        # 병렬 처리 (semaphore는 KISClient 내부에서 관리)
        import asyncio

        async def fetch_and_update(etf: dict):
            ticker = etf["ticker"]
            etf_id = etf["id"]
            try:
                res = await kis_client.get_etf_basic_info(ticker)
                if not res:
                    return

                info = {}

                # NAV
                if res.get("nav"):
                    try:
                        info["nav"] = float(res["nav"])
                    except ValueError:
                        pass

                # AUM: etf_ntas_ttam 단위 = 억원 → 원으로 변환 (×1억)
                if res.get("etf_ntas_ttam"):
                    try:
                        info["aum"] = int(res["etf_ntas_ttam"]) * 100_000_000
                    except ValueError:
                        pass

                # 운용사 (브랜드명으로 변환)
                if res.get("mbcr_name"):
                    raw = res["mbcr_name"].replace("(ETF)", "").strip()
                    if "삼성" in raw:
                        info["asset_manager"] = "KODEX"
                    elif "미래에셋" in raw:
                        info["asset_manager"] = "TIGER"
                    else:
                        info["asset_manager"] = raw

                # 상장일
                if res.get("stck_lstn_date") and res["stck_lstn_date"] != "0":
                    try:
                        info["listing_date"] = datetime.strptime(res["stck_lstn_date"], "%Y%m%d").date()
                    except ValueError:
                        pass

                # 배당주기
                cycle = str(res.get("etf_dvdn_cycl", "")).strip()
                if cycle:
                    info["dividend_freq"] = dividend_freq_map.get(cycle, "NONE")

                # 대표 섹터 → Java EtfSector enum 값으로 정규화
                kis_sector = res.get("etf_rprs_bstp_kor_isnm", "")
                etf_name_for_sector = res.get("prdt_name", "")
                info["sector"] = _normalize_etf_sector(kis_sector, etf_name_for_sector)

                # 카테고리 (ETF(실물복제/수익증권) 등)
                if res.get("bstp_kor_isnm"):
                    info["category"] = res["bstp_kor_isnm"]

                # 총보수율 (expense_ratio): etf_tot_fee 필드 (예: "0.0099")
                if res.get("etf_tot_fee"):
                    try:
                        info["expense_ratio"] = float(res["etf_tot_fee"])
                    except ValueError:
                        pass

                # 배당수익률: etf_dvdn_per 필드
                if res.get("etf_dvdn_per"):
                    try:
                        dv = float(res["etf_dvdn_per"])
                        if dv > 0:
                            info["dividend_yield"] = dv
                    except ValueError:
                        pass

                if info:
                    await self.etf_repository.update_etf_advanced_info(etf_id, info)
                    logger.info(f"[{ticker}] 메타데이터 업데이트 완료: {list(info.keys())}")

            except Exception as e:
                logger.error(f"[{ticker}] 메타데이터 동기화 에러: {e}")

        tasks = [fetch_and_update(etf) for etf in active_etfs]
        await asyncio.gather(*tasks, return_exceptions=True)

        await self.etf_repository.db.commit()
        logger.info(f"=== ETF 메타데이터 동기화 완료: {len(active_etfs)}개 처리 ===")

    async def sync_stock_fundamentals(self):
        """
        pykrx get_market_fundamental을 이용해 KOSPI/KOSDAQ 전 종목의
        PER, PBR을 일괄 조회하고 stock 테이블을 업데이트합니다.
        ROE = PBR / PER 으로 계산합니다.

        최신 영업일 기준으로 조회 (장 미개장/휴장일 시 자동으로 이전 영업일 데이터 반환됨)
        """
        import datetime as dt
        import pandas as pd

        today = dt.date.today()

        # 과거로 소급하며 영업일 데이터 찾기 (최대 5일 전까지 시도)
        for days_back in range(6):
            target_date = today - dt.timedelta(days=days_back)
            date_str = target_date.strftime("%Y%m%d")

            logger.info(f"=== 종목 재무지표(PER/PBR/ROE) 동기화 시작 ({date_str}) ===")
            fundamentals = await self.pykrx_client.get_stocks_fundamental_batch(date_str)

            if fundamentals:
                await self.stock_repository.bulk_update_fundamentals(fundamentals)
                logger.info(f"=== 종목 재무지표 동기화 완료: {len(fundamentals)}개 업데이트 ===")
                return

        logger.warning("최근 5일간 pykrx에서 조회된 재무지표 데이터가 없습니다.")

    async def sync_etf_fundamentals(self):
        """
        ETF 구성종목(etf_compositions)의 비중 가중평균으로 ETF의 per, pbr, roe를 계산합니다.
        roe = pbr / per
        """
        from sqlalchemy import select, text
        from app.models.etf import ETF

        logger.info("=== ETF 재무지표(PER/PBR/ROE) 가중평균 계산 시작 ===")

        # 구성종목 PER/PBR이 있는 ETF들의 가중평균 계산 (SQL로 효율적 처리)
        stmt = text("""
            SELECT
                esc.etf_id,
                SUM(s.per * esc.weight_pct / 100.0) AS weighted_per,
                SUM(s.pbr * esc.weight_pct / 100.0) AS weighted_pbr
            FROM etf_stock_composition esc
            JOIN stock s ON s.id = esc.stock_id
            WHERE s.per IS NOT NULL AND s.per > 0
              AND s.pbr IS NOT NULL AND s.pbr > 0
              AND esc.base_date = (
                  SELECT MAX(base_date) FROM etf_stock_composition WHERE etf_id = esc.etf_id
              )
            GROUP BY esc.etf_id
            HAVING SUM(esc.weight_pct) > 0
        """)

        result = await self.etf_repository.db.execute(stmt)
        rows = result.fetchall()

        if not rows:
            logger.warning("ETF 재무지표 계산에 필요한 구성종목 데이터가 없습니다.")
            return

        from sqlalchemy import update
        updated = 0
        for row in rows:
            etf_id = row[0]
            w_per = float(row[1]) if row[1] else None
            w_pbr = float(row[2]) if row[2] else None

            if not w_per or not w_pbr or w_per == 0:
                continue

            w_roe = round(w_pbr / w_per * 100, 4)
            stmt_upd = (
                update(ETF)
                .where(ETF.id == etf_id)
                .values(
                    per=round(w_per, 2),
                    pbr=round(w_pbr, 2),
                    roe=w_roe
                )
            )
            await self.etf_repository.db.execute(stmt_upd)
            updated += 1

        await self.etf_repository.db.commit()
        logger.info(f"=== ETF 재무지표 계산 완료: {updated}개 ETF 업데이트 ===")

    async def sync_etf_risk_type(self):
        """
        ETF 속성(is_leveraged, is_inverse, is_derivatives, is_krx_only, sector/name 키워드)
        기반으로 risk_type을 계산합니다.

        등급 기준 (RiskType 참조):
          AGGRESSIVE(5) - 레버리지 ETF
          ACTIVE(4)     - 인버스/파생/해외자산 ETF
          MODERATE(3)   - 일반 국내 주식 ETF (기본값)
          STABLE(2)     - 채권/혼합 ETF
          CONSERVATIVE(1) - 국채/MMF/단기채 ETF
        """
        from sqlalchemy import select, update
        from app.models.etf import ETF

        logger.info("=== ETF 위험유형(risk_type) 계산 시작 ===")

        stmt = select(ETF.id, ETF.name, ETF.is_leveraged, ETF.is_inverse,
                      ETF.is_derivatives, ETF.is_krx_only, ETF.sector, ETF.category)
        result = await self.etf_repository.db.execute(stmt)
        etfs = result.fetchall()

        conservative_keywords = ['국채', 'MMF', '단기채', '통안채', 'CD', 'RP', 'A-', 'AA-']
        stable_keywords = ['채권', '회사채', '하이일드', '인플레', '물가']

        updated = 0
        for etf in etfs:
            name = etf.name or ""
            sector = etf.sector or ""
            category = etf.category or ""
            combined = f"{name} {sector} {category}"

            if etf.is_leveraged:
                risk_type = "AGGRESSIVE"
            elif etf.is_inverse or etf.is_derivatives or etf.is_krx_only is False:
                risk_type = "ACTIVE"
            elif any(k in combined for k in conservative_keywords):
                risk_type = "CONSERVATIVE"
            elif any(k in combined for k in stable_keywords):
                risk_type = "STABLE"
            else:
                risk_type = "MODERATE"

            stmt_upd = update(ETF).where(ETF.id == etf.id).values(risk_type=risk_type)
            await self.etf_repository.db.execute(stmt_upd)
            updated += 1

        await self.etf_repository.db.commit()
        logger.info(f"=== ETF 위험유형 계산 완료: {updated}개 ETF 업데이트 ===")

    async def sync_etf_other_composition(self):
        """pykrx PDF에서 비주식 구성종목(채권/선물/현금 등)을 etf_other_composition에 저장.

        - 6자리 숫자 종목(일반 주식)은 제외 → etf_stock_composition 에서 관리
        - KRW / 채권 / 선물 / 원자재 등을 asset_type으로 분류하여 저장
        - ETF 별로 DELETE → INSERT (항상 최신 PDF 기준 덮어쓰기)
        """
        import re
        import asyncio
        from sqlalchemy import delete
        from app.models.etf import EtfOtherComposition

        def _classify_asset_type(ticker: str, name: str) -> str:
            t = ticker.upper()
            n = name
            if t == 'KRW' or '현금' in n or 'CASH' in t:
                return 'CASH'
            if any(k in n for k in ['채권', '국고채', '통안채', '회사채', 'RP', ' CD', ' CP', 'BOND']):
                return 'BOND'
            if any(k in n for k in ['선물', 'FUTURES', 'FORWARD', 'SWAP']) or re.match(r'^[A-Z]\d{5}$', t):
                return 'FUTURES'
            if any(k in n for k in ['금 선물', '원유', '오일', 'OIL', 'GOLD', '원자재', '구리', '천연가스']):
                return 'COMMODITY'
            return 'OTHER'

        def _classify_identifier_type(ticker: str) -> str:
            t = ticker.upper()
            if t == 'KRW':
                return 'INTERNAL_CODE'
            if re.match(r'^KR[A-Z0-9]{10}$', t):  # ISIN 형식: KR + 10자리
                return 'ISIN'
            if re.match(r'^[A-Z]\d{5}$', t):       # KRX 선물코드: 알파벳 + 5자리
                return 'KRX_CODE'
            return 'INTERNAL_CODE'

        active_etfs = await self.etf_repository.get_active_etfs()
        if not active_etfs:
            logger.info("비주식 구성종목 동기화: 활성 ETF 없음")
            return

        logger.info(f"=== ETF 비주식 구성종목 동기화 시작: {len(active_etfs)}개 ETF ===")
        total_saved = 0
        _krx_sem = asyncio.Semaphore(5)

        async def _sync_one(etf: dict):
            nonlocal total_saved
            ticker = etf["ticker"]
            etf_id = etf["id"]

            async with _krx_sem:
                await asyncio.sleep(0.2)
                pdf_infos = await self.pykrx_client.get_etf_pdf_info(ticker)

            if not pdf_infos:
                return

            # 비주식 항목만 필터링
            other_items = []
            for pdf in pdf_infos:
                pdf_ticker = pdf["ticker"].strip()
                pdf_name = pdf["name"].strip()
                # 6자리 숫자 → 일반 주식, 제외
                if pdf_ticker.isdigit() and len(pdf_ticker) == 6:
                    continue
                asset_type = _classify_asset_type(pdf_ticker, pdf_name)
                identifier_type = _classify_identifier_type(pdf_ticker)
                other_items.append(EtfOtherComposition(
                    etf_id=etf_id,
                    asset_type=asset_type,
                    asset_name=pdf_name[:50],
                    identifier_type=identifier_type,
                    identifier_value=pdf_ticker[:30],
                    weight=round(pdf.get("weight", 0.0), 3),
                    market_value=pdf.get("market_value", 0),
                ))

            # DELETE → INSERT
            await self.etf_repository.db.execute(
                delete(EtfOtherComposition).where(EtfOtherComposition.etf_id == etf_id)
            )
            if other_items:
                self.etf_repository.db.add_all(other_items)
                total_saved += len(other_items)
            logger.debug(f"[{ticker}] 비주식 구성종목 {len(other_items)}개 저장")

        await asyncio.gather(*[_sync_one(etf) for etf in active_etfs], return_exceptions=True)
        await self.etf_repository.db.commit()
        logger.info(f"=== ETF 비주식 구성종목 동기화 완료: {total_saved}건 저장 ===")
