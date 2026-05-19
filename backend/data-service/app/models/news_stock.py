"""뉴스-종목 매핑 모델 (네이버 증권 종목뉴스 기반)"""
from sqlalchemy import Column, BigInteger, TIMESTAMP, ForeignKey, func
from sqlalchemy.orm import relationship
from app.database import Base


class NewsStockMapping(Base):
    """뉴스-종목 매핑 테이블 (ERD: news_stock_mapping)

    네이버 증권 종목뉴스 크롤링 결과 저장
    - 하나의 뉴스가 여러 종목에 매핑될 수 있음 (N:M)
    - 네이버가 이미 뉴스-종목 관련성을 분석해놓음
    """
    __tablename__ = "news_stock_mapping"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    news_id = Column(BigInteger, ForeignKey("news_article.id", ondelete="CASCADE"), nullable=False)
    company_id = Column(BigInteger, ForeignKey("company_info.id", ondelete="CASCADE"), nullable=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    # Relationships
    news = relationship("NewsArticle", backref="stock_mappings")
    company = relationship("CompanyInfo", backref="news_mappings")
