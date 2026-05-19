"""벤치마크 지수 (KOSPI 등) 모델"""
from sqlalchemy import Column, BigInteger, DECIMAL, String, Date, TIMESTAMP
from sqlalchemy.sql import func

from app.database import Base

class BenchmarkIndexPrice(Base):
    """벤치마크 지수 가격 테이블 (ERD: benchmark_index_price)"""
    __tablename__ = "benchmark_index_price"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    close = Column(DECIMAL(14, 2))
    market_type = Column(String(50))  # 'KOSPI', 'NASDAQ'
    trading_date = Column(Date)

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
    updated_at = Column(TIMESTAMP(timezone=True), server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<BenchmarkIndexPrice(market_type={self.market_type}, date={self.trading_date}, close={self.close})>"
