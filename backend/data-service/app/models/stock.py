from sqlalchemy import Column, BigInteger, String, DECIMAL, Boolean, Date, TIMESTAMP, Integer, ForeignKey, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.database import Base


class Stock(Base):
    """주식 테이블 (ERD: stock)"""
    __tablename__ = "stock"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    company_id = Column(BigInteger, ForeignKey("company_info.id", ondelete="SET NULL"))
    ticker = Column(String(20), nullable=False, unique=True)
    description = Column(Text)
    close = Column(DECIMAL(14, 2))
    listing_date = Column(Date)
    face_value = Column(Integer)
    listed_shares = Column(BigInteger)
    market_type = Column(String(20))  # KOSPI / KOSDAQ / NYSE / NASDAQ 등

    # 재무 지표 (pykrx get_market_fundamental 기반, roe = pbr / per)
    per = Column(DECIMAL(8, 2))
    pbr = Column(DECIMAL(8, 2))
    roe = Column(DECIMAL(8, 2))

    is_active = Column(Boolean, default=True)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    # Relationships
    company = relationship("CompanyInfo", backref="stocks")

    def __repr__(self):
        return f"<Stock(ticker={self.ticker}, company_id={self.company_id})>"
