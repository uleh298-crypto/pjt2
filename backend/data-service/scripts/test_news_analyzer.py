"""
GPT-4o 뉴스 분석 테스트 스크립트

사용법:
    cd backend/data-service
    python -m scripts.test_news_analyzer
"""
import sys
import asyncio
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from app.database import SessionLocal
from app.models.news import NewsArticle
from app.models.news_impact import NewsImpact
from app.services.news_impact_analyzer import NewsImpactAnalyzer


async def test_single_news_analysis():
    """단일 뉴스 분석 테스트"""
    print("\n" + "=" * 60)
    print("1. 단일 뉴스 분석 테스트")
    print("=" * 60)

    db = SessionLocal()

    # 테스트용 가짜 뉴스 생성
    test_news = NewsArticle(
        id=99999,
        title="삼성전자, AI 반도체에 10조원 투자 발표",
        content="삼성전자가 AI 반도체 생산 확대를 위해 향후 3년간 10조원을 투자한다고 발표했다. "
                "이번 투자로 HBM 및 차세대 메모리 반도체 생산능력을 2배 이상 확대할 계획이다. "
                "업계에서는 SK하이닉스와의 경쟁이 더욱 치열해질 것으로 전망하고 있다.",
        source="한국경제",
        source_url="https://test.com/1",
        published_at="2025-01-17 09:00:00"
    )

    try:
        analyzer = NewsImpactAnalyzer(db)

        # API 키 확인
        if not analyzer.llm.is_configured():
            print("OpenAI API 키가 설정되지 않았습니다.")
            print("실제 분석 대신 모의 결과를 보여줍니다.\n")

            # 모의 결과
            mock_result = {
                "impacts": [
                    {"target": "삼성전자", "type": "company", "score": 0.8, "reason": "AI 반도체 대규모 투자"},
                    {"target": "SK하이닉스", "type": "company", "score": 0.2, "reason": "경쟁 심화 우려"},
                    {"target": "IT_SEMI", "type": "industry", "score": 0.6, "reason": "반도체 업황 개선 기대"}
                ],
                "summary": [
                    "삼성전자 AI 반도체에 10조원 투자 발표",
                    "HBM 및 메모리 반도체 생산능력 2배 확대",
                    "SK하이닉스와 경쟁 심화 전망"
                ],
                "keywords": ["AI반도체", "삼성전자", "HBM", "투자"]
            }

            print(f"뉴스: {test_news.title}\n")
            print("예상 분석 결과:")
            print("-" * 40)

            for impact in mock_result["impacts"]:
                emoji = "📈" if impact["score"] > 0 else "📉" if impact["score"] < 0 else "➖"
                print(f"  {emoji} {impact['target']} ({impact['type']}): {impact['score']:+.1f}")
                print(f"     └ {impact['reason']}")

            print("\n요약:")
            for bullet in mock_result["summary"]:
                print(f"  • {bullet}")

            print(f"\n키워드: {', '.join(mock_result['keywords'])}")
            return

        # 실제 분석
        print(f"분석 대상: {test_news.title}\n")

        result = await analyzer.analyze_news(test_news)

        if result.success:
            print("분석 결과:")
            print("-" * 40)

            for impact in result.impacts or []:
                emoji = "📈" if impact["score"] > 0 else "📉" if impact["score"] < 0 else "➖"
                print(f"  {emoji} {impact['target']} ({impact['type']}): {impact['score']:+.1f}")
                print(f"     └ {impact.get('reason', '')}")

            if result.summary:
                print("\n요약:")
                for bullet in result.summary:
                    print(f"  • {bullet}")

            if result.keywords:
                print(f"\n키워드: {', '.join(result.keywords)}")
        else:
            print(f"분석 실패: {result.error}")

        await analyzer.close()

    finally:
        db.close()


async def test_db_news_analysis():
    """DB에 있는 실제 뉴스 분석 테스트"""
    print("\n" + "=" * 60)
    print("2. DB 뉴스 분석 테스트")
    print("=" * 60)

    db = SessionLocal()

    try:
        # 최근 미분석 뉴스 조회
        news = db.query(NewsArticle).filter(
            ~NewsArticle.id.in_(
                db.query(NewsImpact.news_id).distinct()
            )
        ).order_by(NewsArticle.created_at.desc()).first()

        if not news:
            print("분석할 뉴스가 없습니다.")
            return

        print(f"대상 뉴스: [{news.id}] {news.title}")
        print(f"출처: {news.source}")
        print(f"본문: {(news.content or '')[:100]}...")
        print()

        analyzer = NewsImpactAnalyzer(db)

        if not analyzer.llm.is_configured():
            print("OpenAI API 키가 설정되지 않았습니다.")
            print(".env 파일에 OPENAI_API_KEY를 설정하세요.")
            return

        print("GPT-4o 분석 중...")
        result = await analyzer.analyze_news(news)

        if result.success:
            print("\n분석 완료!")
            print("-" * 40)

            if result.impacts:
                for impact in result.impacts:
                    emoji = "📈" if impact["score"] > 0 else "📉" if impact["score"] < 0 else "➖"
                    print(f"  {emoji} {impact['target']} ({impact['type']}): {impact['score']:+.1f}")
            else:
                print("  (관련 회사/산업 없음)")

            # DB 저장 여부 확인
            save = input("\nDB에 저장하시겠습니까? (y/n): ").strip().lower()
            if save == 'y':
                analyzer._save_impacts(news, result)
                print("저장 완료!")
        else:
            print(f"분석 실패: {result.error}")

        await analyzer.close()

    finally:
        db.close()


async def test_batch_analysis():
    """일괄 분석 테스트"""
    print("\n" + "=" * 60)
    print("3. 일괄 분석 테스트")
    print("=" * 60)

    db = SessionLocal()

    try:
        analyzer = NewsImpactAnalyzer(db)

        if not analyzer.llm.is_configured():
            print("OpenAI API 키가 설정되지 않았습니다.")
            return

        # 최대 5건만 테스트
        print("미분석 뉴스 5건 분석 중...\n")
        stats = await analyzer.analyze_unprocessed(limit=5)

        print(f"\n결과:")
        print(f"  전체: {stats['total']}건")
        print(f"  성공: {stats['success']}건")
        print(f"  영향없음: {stats['no_impact']}건")
        print(f"  실패: {stats['failed']}건")

        await analyzer.close()

    finally:
        db.close()


def show_menu():
    """메뉴 표시"""
    print("\n" + "=" * 60)
    print("GPT-4o 뉴스 분석 테스트")
    print("=" * 60)
    print("1. 단일 뉴스 분석 테스트 (모의)")
    print("2. DB 뉴스 분석 테스트 (실제 API 호출)")
    print("3. 일괄 분석 테스트")
    print("0. 종료")
    print("-" * 60)


async def main():
    while True:
        show_menu()
        choice = input("선택: ").strip()

        if choice == "1":
            await test_single_news_analysis()
        elif choice == "2":
            await test_db_news_analysis()
        elif choice == "3":
            await test_batch_analysis()
        elif choice == "0":
            print("종료합니다.")
            break
        else:
            print("잘못된 선택입니다.")

        input("\nEnter를 눌러 계속...")


if __name__ == "__main__":
    asyncio.run(main())
