"""AI 프롬프트 모델"""
from sqlalchemy import Column, BigInteger, String, Text, Boolean, TIMESTAMP
from sqlalchemy.sql import func

from app.database import Base


class AIPrompt(Base):
    """AI 프롬프트 테이블 (ERD: ai_prompt)"""
    __tablename__ = "ai_prompt"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    name = Column(String(50), nullable=False)  # 'news_analysis', 'portfolio_feedback'
    version = Column(String(20), nullable=False)  # 'v1.0', 'v1.1'
    prompt_template = Column(Text, nullable=False)
    description = Column(String(200))
    is_active = Column(Boolean, default=False)
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<AIPrompt(name={self.name}, version={self.version}, is_active={self.is_active})>"
