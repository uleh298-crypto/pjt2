"""AI 서비스 통합 테스트 스크립트"""
import asyncio
import sys
sys.path.insert(0, '.')

from app.database import SessionLocal
from app.models.news import NewsArticle
from app.models.news_industry import NewsIndustryInfluence
from app.models.portfolio_feedback import PortfolioAIFeedback
from app.services.llm_service import LLMService
from app.services.news_analyzer import NewsAnalyzer
from app.services.portfolio_analyzer import PortfolioAnalyzer


async def test_news_analysis():
    """뉴스 분석 테스트"""
    print("\n" + "=" * 60)
    print("1. 뉴스 분석 테스트 (news_analysis)")
    print("=" * 60)

    db = SessionLocal()
    analyzer = NewsAnalyzer(db)

    try:
        # 미분석 뉴스 조회
        from sqlalchemy import func
        news = db.query(NewsArticle).filter(
            NewsArticle.keywords == None,
            NewsArticle.content != None,
            func.length(NewsArticle.content) >= 100
        ).first()

        if not news:
            # 이미 분석된 뉴스 조회
            news = db.query(NewsArticle).filter(
                NewsArticle.keywords != None
            ).first()
            if news:
                print(f"이미 분석된 뉴스 사용: {news.title[:40]}...")
                print(f"  keywords: {news.keywords}")
                print(f"  content_summary: {news.content_summary}")
                return True
            else:
                print("분석할 뉴스가 없습니다.")
                return False

        print(f"분석 대상: {news.title[:50]}...")
        success = await analyzer.analyze_news(news)

        if success:
            print(f"\n분석 결과:")
            print(f"  keywords: {news.keywords}")
            print(f"  content_summary: {news.content_summary}")

            influences = db.query(NewsIndustryInfluence).filter(
                NewsIndustryInfluence.news_id == news.id
            ).all()
            print(f"  industry_influence: {len(influences)}개")
            for inf in influences:
                print(f"    - {inf.industry_code}: {inf.relevance_score} ({inf.sentiment})")
            return True
        else:
            print("분석 실패")
            return False

    finally:
        await analyzer.close()
        db.close()


async def test_portfolio_analysis():
    """포트폴리오 분석 테스트"""
    print("\n" + "=" * 60)
    print("2. 포트폴리오 분석 테스트 (portfolio_feedback)")
    print("=" * 60)

    db = SessionLocal()
    analyzer = PortfolioAnalyzer(db)

    # 테스트용 포트폴리오 데이터
    test_portfolio = {
        "invest_amount": 10000000,
        "etf_list": [
            {
                "name": "KODEX 반도체",
                "weight_pct": 30.0,
                "sector": "반도체",
                "strategy_type": "THEME",
                "risk_grade": "HIGH_RISK",
                "dividend_freq": "NONE"
            },
            {
                "name": "TIGER 미국S&P500",
                "weight_pct": 40.0,
                "sector": "미국주식",
                "strategy_type": "MARKET",
                "risk_grade": "MODERATE",
                "dividend_freq": "QUARTERLY"
            },
            {
                "name": "KODEX 배당성장",
                "weight_pct": 30.0,
                "sector": "배당",
                "strategy_type": "DIVIDEND",
                "risk_grade": "STABLE",
                "dividend_freq": "MONTHLY"
            }
        ],
        "metrics": {
            "avg_expense_ratio": 0.25,
            "expected_dividend_yield": 2.8,
            "weighted_volatility": 18.5,
            "sector_concentration": "보통"
        }
    }

    try:
        print("테스트 포트폴리오:")
        for etf in test_portfolio["etf_list"]:
            print(f"  - {etf['name']} ({etf['weight_pct']}%)")

        # 테스트용 user_id = 1 (없으면 에러 발생할 수 있음)
        # 실제 환경에서는 존재하는 user_id 사용
        feedback = await analyzer.analyze_portfolio(
            user_id=1,
            portfolio_data=test_portfolio
        )

        if feedback:
            print(f"\n분석 결과:")
            print(f"  headline: {feedback.headline}")
            print(f"  sub_headline: {feedback.sub_headline}")
            print(f"  keywords: {feedback.keywords}")
            print(f"  analysis: {feedback.analysis[:200]}..." if feedback.analysis else "  analysis: None")
            return True
        else:
            print("포트폴리오 분석 실패 (user_id=1 미존재 가능)")
            return False

    except Exception as e:
        print(f"오류 발생: {e}")
        return False

    finally:
        await analyzer.close()
        db.close()


async def test_prompt_check():
    """프롬프트 확인"""
    print("\n" + "=" * 60)
    print("0. 프롬프트 상태 확인")
    print("=" * 60)

    db = SessionLocal()
    llm = LLMService(db)

    prompts = ["news_analysis", "portfolio_feedback", "news_timeline"]

    for name in prompts:
        prompt = llm.get_prompt(name)
        if prompt:
            print(f"  {name}: v{prompt.version} (active={prompt.is_active})")
        else:
            print(f"  {name}: 없음!")

    await llm.close()
    db.close()


async def show_summary():
    """분석 결과 요약"""
    print("\n" + "=" * 60)
    print("분석 결과 요약")
    print("=" * 60)

    db = SessionLocal()

    try:
        # 뉴스 분석 결과
        analyzed_news = db.query(NewsArticle).filter(
            NewsArticle.keywords != None
        ).count()
        print(f"\n뉴스 분석 완료: {analyzed_news}건")

        # 산업 영향력
        industry_count = db.query(NewsIndustryInfluence).count()
        print(f"산업 영향력 레코드: {industry_count}건")

        # 포트폴리오 피드백
        feedback_count = db.query(PortfolioAIFeedback).count()
        print(f"포트폴리오 AI 피드백: {feedback_count}건")

        # 최근 피드백
        recent_feedback = db.query(PortfolioAIFeedback).order_by(
            PortfolioAIFeedback.created_at.desc()
        ).first()

        if recent_feedback:
            print(f"\n최근 포트폴리오 피드백:")
            print(f"  headline: {recent_feedback.headline}")
            print(f"  sub_headline: {recent_feedback.sub_headline}")

    finally:
        db.close()


async def main():
    print("=" * 60)
    print("AI 서비스 통합 테스트")
    print("=" * 60)

    # 0. 프롬프트 확인
    await test_prompt_check()

    # 1. 뉴스 분석 테스트
    await test_news_analysis()

    # 2. 포트폴리오 분석 테스트
    await test_portfolio_analysis()

    # 결과 요약
    await show_summary()


if __name__ == "__main__":
    asyncio.run(main())
