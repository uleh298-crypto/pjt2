"""
상위 100개 ETF 구성종목 뉴스 크롤링 + AI 분석

1. 뉴스 크롤링
2. AI 분석 (요약, 키워드, ETF 추천) 자동 실행

사용법:
    cd backend/data-service
    python -m scripts.crawl_top100_news
"""
import sys
import asyncio
import logging
from pathlib import Path
from datetime import datetime

sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import text
from app.database import SessionLocal
from app.scrapers.stock_news_scraper import StockNewsScraper
from app.services.news_analyzer import analyze_unprocessed_news

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("news_crawl_top100.log", encoding="utf-8")
    ]
)
logger = logging.getLogger(__name__)


async def crawl_top100_etf_stocks():
    """상위 100개 ETF 구성종목 뉴스 크롤링"""
    db = SessionLocal()

    try:
        # 상위 100개 ETF의 구성종목 (중복 제거)
        result = db.execute(text("""
            SELECT DISTINCT c.stock_code, c.stock_name
            FROM company_info c
            JOIN stock s ON s.company_id = c.id
            JOIN etf_stock_composition esc ON esc.stock_id = s.id
            JOIN etf e ON e.id = esc.etf_id
            WHERE c.stock_code IS NOT NULL
              AND e.is_active = true
              AND e.id IN (
                  SELECT id FROM etf
                  WHERE is_active = true
                  ORDER BY aum DESC NULLS LAST
                  LIMIT 100
              )
            ORDER BY c.stock_code
        """))
        stocks = result.fetchall()

        total = len(stocks)
        logger.info(f"=== 상위 100개 ETF 구성종목 뉴스 크롤링 ===")
        logger.info(f"대상 종목: {total}개")
        logger.info(f"시작 시간: {datetime.now()}")

        total_stats = {"total": 0, "new": 0, "mapped": 0}

        async with StockNewsScraper(db) as scraper:
            for i, (stock_code, stock_name) in enumerate(stocks, 1):
                try:
                    logger.info(f"[{i}/{total}] {stock_code} {stock_name}")

                    stats = await scraper.scrape_stock_news(
                        stock_code=stock_code,
                        max_articles=3
                    )

                    total_stats["total"] += stats["total"]
                    total_stats["new"] += stats["new"]
                    total_stats["mapped"] += stats["mapped"]

                    if stats["new"] > 0:
                        logger.info(f"  -> 신규 {stats['new']}건")

                except Exception as e:
                    logger.error(f"  [!] 실패: {e}")
                    db.rollback()
                    continue

        logger.info("=" * 50)
        logger.info("=== 상위 100개 ETF 구성종목 크롤링 완료 ===")
        logger.info(f"종료 시간: {datetime.now()}")
        logger.info(f"처리: {total_stats['total']}건")
        logger.info(f"신규: {total_stats['new']}건")
        logger.info(f"매핑추가: {total_stats['mapped']}건")

        # DB 확인
        result = db.execute(text("SELECT COUNT(*) FROM news_article"))
        news_count = result.fetchone()[0]
        logger.info(f"DB 뉴스 총: {news_count}건")

        # AI 분석 자동 실행
        if total_stats["new"] > 0:
            logger.info("=" * 50)
            logger.info("=== AI 뉴스 분석 시작 ===")
            analyzed = await analyze_unprocessed_news(db, limit=total_stats["new"] + 50)
            logger.info(f"=== AI 분석 완료: {analyzed}건 처리 ===")

    finally:
        db.close()


def main():
    asyncio.run(crawl_top100_etf_stocks())


if __name__ == "__main__":
    main()
