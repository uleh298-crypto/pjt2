"""뉴스-ETF 영향력 모델"""
from sqlalchemy import Column, BigInteger, String, Text, DECIMAL, TIMESTAMP, Boolean, ForeignKey
from sqlalchemy.sql import func

from app.database import Base


class NewsETFInfluence(Base):
    """뉴스-ETF 영향력 테이블 (ERD: news_etf_influence)

    2차 분석 결과: 뉴스 → ETF 영향력 매핑
    산업 영향력(news_industry_influence) 기반으로 ETF에 매핑
    """
    __tablename__ = "news_etf_influence"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    news_id = Column(BigInteger, ForeignKey("news_article.id", ondelete="CASCADE"), nullable=False)
    etf_id = Column(BigInteger, ForeignKey("etf.id", ondelete="CASCADE"), nullable=False)

    # 영향력 점수
    influence_score = Column(DECIMAL(5, 4))  # 0.0000 ~ 1.0000
    influence_type = Column(String(20))  # POSITIVE / NEGATIVE / NEUTRAL

    # 타임라인용 (UI 표시)
    timeline_title = Column(String(100))  # "연준 기준금리 동결 발표"
    timeline_summary = Column(String(200))  # "시장 예상치 부합, 기술주 중심 반등세"

    # 상세 분석
    analysis_reason = Column(Text)  # 상세 분석 근거

    # 실제 데이터 기반 검증
    actual_change_rate = Column(DECIMAL(8, 4))  # 뉴스 발행 후 ETF 실제 변동률
    verified_at = Column(TIMESTAMP(timezone=True))  # 검증 시점
    is_verified = Column(Boolean, default=False)  # 실제 데이터로 검증됨

    created_at = Column(TIMESTAMP(timezone=True), server_default=func.now())

    def __repr__(self):
        return f"<NewsETFInfluence(news_id={self.news_id}, etf_id={self.etf_id}, score={self.influence_score})>"
