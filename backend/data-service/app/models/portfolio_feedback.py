"""포트폴리오 AI 피드백 모델"""
from sqlalchemy import Column, BigInteger, String, Text, TIMESTAMP
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func

from app.database import Base


class PortfolioAIFeedback(Base):
    """포트폴리오 AI 피드백 테이블 (ERD: portfolio_ai_feedback)

    Note: DB에 FK 제약조건 있음 (user_id → user.id, prompt_id → ai_prompt.id)
          SQLAlchemy 모델에서는 FK 참조 생략 (User 모델 미정의)
    """
    __tablename__ = "portfolio_ai_feedback"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, nullable=False)  # FK: user.id (DB 레벨)
    portfolio_snapshot_id = Column(BigInteger)
    prompt_id = Column(BigInteger)  # FK: ai_prompt.id (DB 레벨)

    # 진단 결과 헤드라인
    headline = Column(String(100))  # "공격적인 수익 추구!"
    sub_headline = Column(String(200))  # "기술주 중심의 로켓 포트폴리오"
    keywords = Column(JSONB)  # ["기술주집중", "고변동성", "성장중심"]

    # 상세 분석
    analysis = Column(Text)  # 종합 분석 결과

    # 메타
    llm_model = Column(String(50))  # 사용된 LLM 모델
    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<PortfolioAIFeedback(user_id={self.user_id}, headline={self.headline})>"
