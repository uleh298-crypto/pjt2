"""LLM 뉴스 분석 테스트 스크립트"""
import asyncio
import sys
sys.path.insert(0, '.')

from app.database import SessionLocal
from app.models.news import NewsArticle
from app.models.news_industry import NewsIndustryInfluence
from app.services.llm_service import LLMService
from app.services.news_analyzer import NewsAnalyzer


async def test_llm_connection():
    """LLM 연결 테스트"""
    print("\n=== LLM 연결 테스트 ===")

    db = SessionLocal()
    llm = LLMService(db)

    if not llm.is_configured():
        print("OpenAI API 키 미설정 - .env에 OPENAI_API_KEY 추가 필요")
        db.close()
        return False

    try:
        # 간단한 테스트 호출
        response = await llm.call_json(
            "당신은 JSON 응답만 하는 도우미입니다.",
            "1+1의 결과를 JSON으로 응답해주세요. 형식: {\"result\": 숫자}"
        )

        if response:
            print(f"LLM 연결 성공: {response}")
            return True
        else:
            print("LLM 응답 없음")
            return False

    finally:
        await llm.close()
        db.close()


async def test_prompt_fetch():
    """프롬프트 조회 테스트"""
    print("\n=== 프롬프트 조회 테스트 ===")

    db = SessionLocal()
    llm = LLMService(db)

    try:
        prompt = llm.get_prompt("news_analysis")
        if prompt:
            print(f"프롬프트 조회 성공:")
            print(f"  - name: {prompt.name}")
            print(f"  - version: {prompt.version}")
            print(f"  - is_active: {prompt.is_active}")
            print(f"  - template 길이: {len(prompt.prompt_template)}자")
            return True
        else:
            print("프롬프트를 찾을 수 없음 (news_analysis, is_active=True)")
            return False

    finally:
        await llm.close()
        db.close()


async def test_news_analysis():
    """뉴스 분석 테스트"""
    print("\n=== 뉴스 분석 테스트 ===")

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
            print("분석할 뉴스가 없습니다.")
            return False

        print(f"분석 대상 뉴스:")
        print(f"  - ID: {news.id}")
        print(f"  - 제목: {news.title[:50]}...")
        print(f"  - 출처: {news.source}")
        print(f"  - 본문 길이: {len(news.content) if news.content else 0}자")

        success = await analyzer.analyze_news(news)

        if success:
            print(f"\n분석 결과:")
            print(f"  - keywords: {news.keywords}")
            print(f"  - content_summary: {news.content_summary}")

            # industry_influence 조회
            influences = db.query(NewsIndustryInfluence).filter(
                NewsIndustryInfluence.news_id == news.id
            ).all()
            print(f"  - industry_influence: {len(influences)}개")
            for inf in influences:
                print(f"    - {inf.industry_code}: {inf.relevance_score} ({inf.sentiment})")
            return True
        else:
            print("뉴스 분석 실패")
            return False

    finally:
        await analyzer.close()
        db.close()


async def test_batch_analysis():
    """일괄 분석 테스트"""
    print("\n=== 일괄 분석 테스트 ===")

    db = SessionLocal()
    analyzer = NewsAnalyzer(db)

    try:
        result = await analyzer.analyze_unprocessed_news(limit=3)
        print(f"분석 결과: {result}")
        return result["success"] > 0

    finally:
        await analyzer.close()
        db.close()


async def show_analysis_results():
    """분석 결과 조회"""
    print("\n=== 분석 결과 조회 ===")

    db = SessionLocal()

    try:
        # 분석 완료된 뉴스
        analyzed = db.query(NewsArticle).filter(
            NewsArticle.keywords != None
        ).order_by(NewsArticle.created_at.desc()).limit(5).all()

        print(f"분석 완료 뉴스: {len(analyzed)}건\n")

        for news in analyzed:
            print(f"[{news.source}] {news.title[:40]}...")
            print(f"  keywords: {news.keywords}")
            if news.content_summary:
                bullets = news.content_summary.get("bullets", [])
                print(f"  summary: {len(bullets)}개 bullet")
            print()

        # 산업 영향력 통계
        print("=== 산업별 뉴스 영향력 통계 ===")
        from sqlalchemy import func as sqlfunc
        stats = db.query(
            NewsIndustryInfluence.industry_code,
            sqlfunc.count(NewsIndustryInfluence.id).label("count"),
            sqlfunc.avg(NewsIndustryInfluence.relevance_score).label("avg_relevance")
        ).group_by(NewsIndustryInfluence.industry_code).all()

        for stat in stats:
            print(f"  {stat.industry_code}: {stat.count}건 (평균 관련도: {stat.avg_relevance:.2f})")

    finally:
        db.close()


async def main():
    print("=" * 60)
    print("LLM 뉴스 분석 테스트")
    print("=" * 60)

    # 1. 프롬프트 조회 테스트
    await test_prompt_fetch()

    # 2. LLM 연결 테스트
    connected = await test_llm_connection()
    if not connected:
        print("\nLLM 연결 실패 - API 키 확인 필요")
        return

    # 3. 단일 뉴스 분석 테스트
    await test_news_analysis()

    # 4. 결과 조회
    await show_analysis_results()


if __name__ == "__main__":
    asyncio.run(main())
