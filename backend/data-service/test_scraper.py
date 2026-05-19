"""뉴스 스크래퍼 테스트 스크립트"""
import asyncio
import sys
sys.path.insert(0, '.')

from app.database import SessionLocal, engine
from app.models.news import NewsArticle
from app.scrapers.news_scraper import GoogleNewsScraper
from app.scrapers.naver_scraper import NaverNewsScraper
from app.scrapers.content_scraper import ContentScraper, enrich_news_content


async def test_google_news():
    """Google News RSS 테스트"""
    print("\n=== Google News RSS 테스트 ===")

    db = SessionLocal()
    scraper = GoogleNewsScraper(db)

    try:
        # 반도체 ETF 키워드로 테스트
        count = await scraper.scrape_by_keyword("반도체 ETF", max_items=3)
        print(f"수집된 뉴스: {count}건")

        # 저장된 뉴스 확인
        news_list = db.query(NewsArticle).order_by(NewsArticle.created_at.desc()).limit(5).all()
        for news in news_list:
            print(f"\n- 제목: {news.title[:50]}...")
            print(f"  출처: {news.source}")
            print(f"  카테고리: {news.category} ({news.category_name})")
            print(f"  content 길이: {len(news.content) if news.content else 0}자")
            print(f"  content_summary: {news.content_summary}")
            print(f"  keywords: {news.keywords}")

    finally:
        await scraper.close()
        db.close()


async def test_naver_news():
    """Naver News API 테스트"""
    print("\n=== Naver News API 테스트 ===")

    db = SessionLocal()
    scraper = NaverNewsScraper(db)

    if not scraper.is_configured():
        print("Naver API 키 미설정 - 테스트 건너뜀")
        db.close()
        return

    try:
        count = await scraper.scrape_by_keyword("ETF 투자", display=3)
        print(f"수집된 뉴스: {count}건")

    finally:
        await scraper.close()
        db.close()


async def test_content_scraper():
    """본문 크롤링 테스트"""
    print("\n=== 본문 크롤링 테스트 ===")

    db = SessionLocal()
    content_scraper = ContentScraper()

    try:
        # 본문이 짧은 뉴스 찾기
        from sqlalchemy import func
        news_to_enrich = db.query(NewsArticle).filter(
            (NewsArticle.content == None) |
            (func.length(NewsArticle.content) < 500)
        ).limit(3).all()

        print(f"본문 보강 대상: {len(news_to_enrich)}건")

        for news in news_to_enrich:
            print(f"\n처리 중: {news.title[:40]}...")
            print(f"  URL: {news.source_url[:60]}...")

            success = await enrich_news_content(news, content_scraper)
            if success:
                db.commit()
                print(f"  ✅ 본문 크롤링 성공 ({len(news.content)}자)")
            else:
                print(f"  ❌ 본문 크롤링 실패 (지원하지 않는 언론사)")

    finally:
        await content_scraper.close()
        db.close()


async def show_results():
    """최종 결과 출력"""
    print("\n=== 최종 결과 ===")

    db = SessionLocal()

    try:
        news_list = db.query(NewsArticle).order_by(NewsArticle.created_at.desc()).limit(10).all()

        print(f"총 저장된 뉴스: {db.query(NewsArticle).count()}건\n")

        for news in news_list:
            content_len = len(news.content) if news.content else 0
            has_full_content = "✅" if content_len > 500 else "⚠️"

            print(f"{has_full_content} [{news.category_name}] [{news.source}] {news.title[:40]}...")
            print(f"   category: {news.category} | content: {content_len}자")
            print()

    finally:
        db.close()


async def main():
    print("=" * 60)
    print("뉴스 스크래퍼 테스트")
    print("=" * 60)

    await test_google_news()
    await test_naver_news()
    await test_content_scraper()
    await show_results()


if __name__ == "__main__":
    asyncio.run(main())
