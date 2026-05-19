"""회사정보 및 산업분류 모델"""
from sqlalchemy import Column, BigInteger, String, Boolean, Date, TIMESTAMP, Integer, Text, ForeignKey
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship

from app.database import Base


class IndustryClassification(Base):
    """산업분류 테이블 (ERD: industry_classification)"""
    __tablename__ = "industry_classification"

    code = Column(String(10), primary_key=True)
    name = Column(String(100), nullable=False)
    level = Column(Integer, nullable=False)  # 1=대, 2=중, 3=소, 4=세분류
    parent_code = Column(String(10), ForeignKey("industry_classification.code", ondelete="SET NULL"))
    group_code = Column(String(20))  # ETF용 그룹 태그
    group_name = Column(String(50))
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    # Self-referencing relationship
    parent = relationship("IndustryClassification", remote_side=[code], backref="children")

    def __repr__(self):
        return f"<Industry(code={self.code}, name={self.name}, group={self.group_code})>"


class CompanyInfo(Base):
    """회사정보 테이블 (ERD: company_info)"""
    __tablename__ = "company_info"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    company_name = Column(String(100), nullable=False)
    industry_code = Column(String(10), ForeignKey("industry_classification.code", ondelete="SET NULL"))
    industry_name = Column(String(100))
    industry_group = Column(String(50))  # 투자테마 그룹 (IT_SEMI, BIO 등)

    ceo_name = Column(String(100))
    homepage = Column(String(200))
    region = Column(String(50))
    is_active = Column(Boolean, default=True)
    corporation_number = Column(String(50))

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())
    updated_at = Column(TIMESTAMP(timezone=True), server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<Company(id={self.id}, name={self.company_name})>"
