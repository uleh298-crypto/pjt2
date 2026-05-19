from sqlalchemy import select, insert, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.benchmark import BenchmarkIndexPrice

class BenchmarkRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_latest_price_date(self, market_type: str):
        stmt = select(func.max(BenchmarkIndexPrice.trading_date)).where(BenchmarkIndexPrice.market_type == market_type)
        result = await self.db.execute(stmt)
        return result.scalar()

    async def save_bulk(self, price_histories):
        if not price_histories:
            return

        stmt = insert(BenchmarkIndexPrice)
        await self.db.execute(stmt, price_histories)
        await self.db.commit()
