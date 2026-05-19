"""포트폴리오 AI 분석 서비스"""
import logging
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session

from app.models.portfolio_feedback import PortfolioAIFeedback
from app.models.ai_prompt import AIPrompt
from app.services.llm_service import LLMService
from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()


class PortfolioAnalyzer:
    """
    포트폴리오 AI 분석 서비스

    - 사용자 포트폴리오 구성을 분석하여 투자 성향 진단
    - headline, sub_headline, keywords, analysis 생성
    """

    PROMPT_NAME = "portfolio_feedback"

    def __init__(self, db: Session):
        self.db = db
        self.llm = LLMService(db)

    async def close(self):
        await self.llm.close()

    def _build_user_message(self, portfolio_data: Dict[str, Any]) -> str:
        """포트폴리오 정보를 프롬프트 입력 형식으로 변환

        Args:
            portfolio_data: {
                "invest_amount": 10000000,
                "etf_list": [
                    {
                        "name": "KODEX 반도체",
                        "weight_pct": 30.0,
                        "sector": "반도체",
                        "strategy_type": "THEME",
                        "risk_grade": "HIGH_RISK",
                        "dividend_freq": "QUARTERLY"
                    },
                    ...
                ],
                "metrics": {
                    "avg_expense_ratio": 0.35,
                    "expected_dividend_yield": 2.5,
                    "weighted_volatility": 25.3,
                    "sector_concentration": "높음"
                }
            }
        """
        invest_amount = portfolio_data.get("invest_amount", 0)
        etf_list = portfolio_data.get("etf_list", [])
        metrics = portfolio_data.get("metrics", {})

        # ETF 목록 문자열 생성
        etf_str_list = []
        for etf in etf_list:
            etf_str = f"""- ETF명: {etf.get('name', 'N/A')}
- 비중: {etf.get('weight_pct', 0)}%
- 섹터: {etf.get('sector', 'N/A')}
- 전략: {etf.get('strategy_type', 'N/A')}
- 위험등급: {etf.get('risk_grade', 'N/A')}
- 배당주기: {etf.get('dividend_freq', 'N/A')}"""
            etf_str_list.append(etf_str)

        etf_composition = "\n\n".join(etf_str_list)

        return f"""[포트폴리오 정보]
투자금액: {invest_amount:,}원

[ETF 구성]
{etf_composition}

[포트폴리오 지표]
- 평균 보수율: {metrics.get('avg_expense_ratio', 0)}%
- 예상 배당수익률: {metrics.get('expected_dividend_yield', 0)}%
- 가중평균 변동성: {metrics.get('weighted_volatility', 0)}%
- 섹터 집중도: {metrics.get('sector_concentration', 'N/A')}"""

    async def analyze_portfolio(
        self,
        user_id: int,
        portfolio_data: Dict[str, Any],
        portfolio_snapshot_id: Optional[int] = None
    ) -> Optional[PortfolioAIFeedback]:
        """
        포트폴리오 분석 및 AI 피드백 생성

        Args:
            user_id: 사용자 ID
            portfolio_data: 포트폴리오 데이터
            portfolio_snapshot_id: 포트폴리오 스냅샷 ID (선택)

        Returns:
            PortfolioAIFeedback 객체 (None if failed)
        """
        if not self.llm.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return None

        user_message = self._build_user_message(portfolio_data)

        try:
            result = await self.llm.analyze_with_prompt(self.PROMPT_NAME, user_message)
            if not result:
                logger.error("포트폴리오 분석 실패")
                return None

            # 프롬프트 ID 조회
            prompt = self.llm.get_prompt(self.PROMPT_NAME)
            prompt_id = prompt.id if prompt else None

            # 피드백 저장
            feedback = PortfolioAIFeedback(
                user_id=user_id,
                portfolio_snapshot_id=portfolio_snapshot_id,
                prompt_id=prompt_id,
                headline=result.get("headline"),
                sub_headline=result.get("sub_headline"),
                keywords=result.get("keywords"),
                analysis=result.get("analysis"),
                llm_model=settings.openai_model
            )

            self.db.add(feedback)
            self.db.commit()
            self.db.refresh(feedback)

            logger.info(f"포트폴리오 분석 완료: user_id={user_id}, headline={feedback.headline}")
            return feedback

        except Exception as e:
            logger.error(f"포트폴리오 분석 중 오류: {e}")
            self.db.rollback()
            return None

    async def get_feedback(self, user_id: int, limit: int = 5) -> List[PortfolioAIFeedback]:
        """사용자의 최근 AI 피드백 조회"""
        return self.db.query(PortfolioAIFeedback).filter(
            PortfolioAIFeedback.user_id == user_id
        ).order_by(PortfolioAIFeedback.created_at.desc()).limit(limit).all()


# API에서 사용할 함수
async def analyze_user_portfolio(
    db: Session,
    user_id: int,
    portfolio_data: Dict[str, Any],
    portfolio_snapshot_id: Optional[int] = None
) -> Optional[PortfolioAIFeedback]:
    """포트폴리오 분석 API 함수"""
    analyzer = PortfolioAnalyzer(db)
    try:
        return await analyzer.analyze_portfolio(user_id, portfolio_data, portfolio_snapshot_id)
    finally:
        await analyzer.close()
