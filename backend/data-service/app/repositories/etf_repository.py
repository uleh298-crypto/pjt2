from sqlalchemy import select, insert
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.dialects.postgresql import insert as pg_insert

from app.models import ETF, ETFPrice
from app.scrapers.pykrx_client import EtfInfo


class EtfRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    def _determine_strategy_type(self, etf_name: str) -> str:
        """ETF 이름 기반으로 strategy_type 판별"""
        name = etf_name.upper()

        # 채권형 (BOND): 채권, 특수채, 국채, 회사채 등
        if any(k in name for k in ["채권", "특수채", "국채", "회사채", "하이일드", "인플레"]):
            return "BOND"

        # 배당형 (DIVIDEND): 배당, 고배당, 배당주 등
        if any(k in name for k in ["배당", "고배당", "배당주"]):
            return "DIVIDEND"

        # 테마형 (THEME): 특정 섹터, 산업, 기업 등
        if any(k in name for k in ["AI", "반도체", "2차전지", "전기차", "바이오", "의약", "헬스", "게임", "카카오",
                                    "통신", "금융", "건설", "조선", "자동차", "화학", "에너지", "전자", "IT",
                                    "인터넷", "미디어", "유통", "식품", "운송", "물류", "부동산", "지주회사"]):
            return "THEME"

        # 기본값: 시장대표 (MARKET)
        return "MARKET"

    # db에 적재 중인 etf 목록
    async def get_etf_tickers(self) -> list[str]:
        stmt = select(ETF.stock_code)
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def save_initial_etf_infos(self, etf_infos: list[EtfInfo]) -> list[dict]:
        if not etf_infos:
            return []

        # 기존 DB의 ticker 목록
        existing_tickers = set(await self.get_etf_tickers())

        # 새로운 데이터만 필터링
        new_infos = [etf for etf in etf_infos if etf.ticker not in existing_tickers]

        if not new_infos:
            return []

        rows = []
        for etf in new_infos:
            is_lev = "레버리지" in etf.etf_name
            is_inv = "인버스" in etf.etf_name

            # strategy_type 판별: MARKET / THEME / DIVIDEND / BOND / DERIVATIVE
            strategy_type = self._determine_strategy_type(etf.etf_name)

            rows.append({
                "stock_code" : etf.ticker,
                "name" : etf.etf_name,
                "asset_manager" : etf.etf_manager,  # KODEX 또는 TIGER
                "is_active": False,
                "is_leveraged": is_lev,
                "is_inverse": is_inv,
                "is_derivatives": is_lev or is_inv,
                "is_krx_only": None,
                "strategy_type": strategy_type,
                "sector": None  # strategy_type이 THEME일 때만 캡처로 채움
            })

        # ON CONFLICT DO NOTHING: unique 제약이나 중복으로 인한 에러 무시
        stmt = pg_insert(ETF).values(rows).on_conflict_do_nothing()
        await self.db.execute(stmt)
        await self.db.flush()

        tickers = [etf_info.ticker for etf_info in new_infos]

        result = await self.db.execute(
            select(ETF.id, ETF.stock_code).where(ETF.stock_code.in_(tickers))
        )
        await self.db.commit()

        return [{"id": row.id, "ticker":row.stock_code} for row in result.all()]

    async def update_krx_status(self, etf_id: int, is_krx_only: bool):
        from sqlalchemy import update
        stmt = update(ETF).where(ETF.id == etf_id).values(is_krx_only=is_krx_only, is_active=is_krx_only)
        await self.db.execute(stmt)
        await self.db.commit()

    async def update_etf_advanced_info(self, etf_id: int, info: dict):
        from sqlalchemy import update
        stmt = update(ETF).where(ETF.id == etf_id).values(**info)
        await self.db.execute(stmt)

    async def get_unchecked_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code, ETF.name).where(ETF.is_krx_only.is_(None))
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code, "name": row.name} for row in result.all()]

    async def get_all_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code, ETF.name)
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code, "name": row.name} for row in result.all()]

    async def get_active_etfs(self) -> list[dict]:
        stmt = select(ETF.id, ETF.stock_code).where(ETF.is_active == True)
        result = await self.db.execute(stmt)
        return [{"id": row.id, "ticker": row.stock_code} for row in result.all()]



class EtfPriceRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def save_bulk(self, price_histories):
        if not price_histories:
            return

            # SQLAlchemy 2.0 Core 스타일의 Bulk Insert
        stmt = insert(ETFPrice)
        await self.db.execute(stmt, price_histories)
        await self.db.commit()

    async def get_latest_price_date(self, etf_id: int):
        from sqlalchemy import func
        stmt = select(func.max(ETFPrice.trade_date)).where(ETFPrice.etf_id == etf_id)
        result = await self.db.execute(stmt)
        return result.scalar()

    async def get_latest_close(self, etf_id: int):
        """직전 저장된 종가 조회 (change_rate 계산용)"""
        stmt = (
            select(ETFPrice.close)
            .where(ETFPrice.etf_id == etf_id)
            .order_by(ETFPrice.trade_date.desc())
            .limit(1)
        )
        result = await self.db.execute(stmt)
        return result.scalar()
