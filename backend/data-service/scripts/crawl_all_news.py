"""
전체 종목 뉴스 크롤링 스크립트 (이번 달)

사용법:
    cd backend/data-service
    python -m scripts.crawl_all_news
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

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("news_crawl.log", encoding="utf-8")
    ]
)
logger = logging.getLogger(__name__)


async def crawl_all_stocks():
    """모든 종목 뉴스 크롤링"""
    db = SessionLocal()

    try:
        # ETF 구성종목에 포함된 종목만 크롤링
        result = db.execute(text("""
            SELECT DISTINCT c.stock_code, c.stock_name
            FROM company_info c
            JOIN stock s ON s.company_id = c.id
            JOIN etf_stock_composition esc ON esc.stock_id = s.id
            WHERE c.stock_code IS NOT NULL
            ORDER BY c.stock_code
        """))
        stocks = result.fetchall()

        total = len(stocks)
        logger.info(f"=== 뉴스 크롤링 시작: {total}개 종목 ===")
        logger.info(f"시작 시간: {datetime.now()}")

        total_stats = {"total": 0, "new": 0, "mapped": 0}

        async with StockNewsScraper(db) as scraper:
            for i, (stock_code, stock_name) in enumerate(stocks, 1):
                try:
                    logger.info(f"[{i}/{total}] {stock_code} {stock_name}")

                    # 종목당 최대 10개 기사 (이번 달)
                    stats = await scraper.scrape_stock_news(
                        stock_code=stock_code,
                        max_articles=10
                    )

                    total_stats["total"] += stats["total"]
                    total_stats["new"] += stats["new"]
                    total_stats["mapped"] += stats["mapped"]

                    if stats["new"] > 0:
                        logger.info(f"  -> 신규 {stats['new']}건")

                    # 진행률 표시 (100개마다)
                    if i % 100 == 0:
                        logger.info(f"=== 진행률: {i}/{total} ({i*100//total}%) ===")
                        logger.info(f"  누적: 총 {total_stats['total']}건, 신규 {total_stats['new']}건")

                except Exception as e:
                    logger.error(f"  [!] 실패: {e}")
                    db.rollback()  # 트랜잭션 롤백
                    continue

        logger.info("=" * 50)
        logger.info("=== 뉴스 크롤링 완료 ===")
        logger.info(f"종료 시간: {datetime.now()}")
        logger.info(f"처리: {total_stats['total']}건")
        logger.info(f"신규: {total_stats['new']}건")
        logger.info(f"매핑추가: {total_stats['mapped']}건")

        # DB 확인
        result = db.execute(text("SELECT COUNT(*) FROM news_article"))
        news_count = result.fetchone()[0]
        logger.info(f"DB 뉴스 총: {news_count}건")

    finally:
        db.close()


def main():
    asyncio.run(crawl_all_stocks())


if __name__ == "__main__":
    main()
