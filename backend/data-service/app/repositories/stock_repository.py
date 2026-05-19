from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.stock import Stock
from app.models.company import CompanyInfo

class StockRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_or_create_company(self, corp_name: str, corp_number: str = None) -> CompanyInfo:
        result = await self.db.execute(select(CompanyInfo).where(CompanyInfo.company_name == corp_name))
        company = result.scalar_one_or_none()
        if not company:
            company = CompanyInfo(company_name=corp_name, corporation_number=corp_number)
            self.db.add(company)
            await self.db.flush()
        else:
            if corp_number and not company.corporation_number:
                company.corporation_number = corp_number
                await self.db.flush()
        return company

    async def update_company_info(self, company_id: int, info: dict):
        result = await self.db.execute(select(CompanyInfo).where(CompanyInfo.id == company_id))
        company = result.scalar_one_or_none()
        if company:
            if info.get("industry_name"):
                company.industry_name = info["industry_name"]
            if info.get("ceo_name"):
                company.ceo_name = info["ceo_name"]
            if info.get("homepage"):
                company.homepage = info["homepage"]
            if info.get("region"):
                company.region = info["region"]
            if info.get("corporation_number"):
                company.corporation_number = info["corporation_number"]
            await self.db.flush()
            
    async def update_stock_description(self, ticker: str, description: str):
        result = await self.db.execute(select(Stock).where(Stock.ticker == ticker))
        stock = result.scalar_one_or_none()
        if stock and description:
            stock.description = description
            await self.db.flush()

    async def get_or_create_stock(self, ticker: str, company_id: int, market_type: str = None) -> Stock:
        result = await self.db.execute(select(Stock).where(Stock.ticker == ticker))
        stock = result.scalar_one_or_none()
        if not stock:
            stock = Stock(ticker=ticker, company_id=company_id, market_type=market_type)
            self.db.add(stock)
            await self.db.flush()
        else:
            if not stock.company_id:
                stock.company_id = company_id
            if market_type and not stock.market_type:
                stock.market_type = market_type
            await self.db.flush()
        return stock

    async def get_stocks_with_empty_company_info(self) -> list[str]:
        stmt = select(Stock.ticker).join(CompanyInfo, Stock.company_id == CompanyInfo.id).where(CompanyInfo.ceo_name.is_(None))
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def get_all_domestic_stock_tickers(self) -> list[str]:
        """KOSPI/KOSDAQ 국내 주식 티커 목록 조회"""
        from sqlalchemy import or_
        stmt = select(Stock.ticker).where(
            Stock.is_active == True,
            or_(Stock.market_type == "KOSPI", Stock.market_type == "KOSDAQ")
        )
        result = await self.db.execute(stmt)
        return list(result.scalars().all())

    async def bulk_update_fundamentals(self, fundamentals: dict[str, dict]):
        """
        {ticker: {"per": float, "pbr": float, "roe": float}} 형태의 딕셔너리로
        stock 테이블의 per, pbr, roe를 일괄 업데이트합니다.
        """
        from sqlalchemy import update
        if not fundamentals:
            return

        for ticker, data in fundamentals.items():
            stmt = (
                update(Stock)
                .where(Stock.ticker == ticker)
                .values(per=data["per"], pbr=data["pbr"], roe=data["roe"])
            )
            await self.db.execute(stmt)

        await self.db.commit()
