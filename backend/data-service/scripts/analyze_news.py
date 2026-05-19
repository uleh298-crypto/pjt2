"""
뉴스 AI 분석 스크립트

미분석 뉴스 기사를 AI로 분석하여:
1. 3줄 요약 생성
2. 키워드 추출
3. 관련 ETF 추천

사용법:
    cd backend/data-service
    python -m scripts.analyze_news [--limit 50]
"""
import sys
import asyncio
import logging
import argparse
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from app.database import SessionLocal
from app.services.news_analyzer import analyze_unprocessed_news

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("news_analysis.log", encoding="utf-8")
    ]
)
logger = logging.getLogger(__name__)


async def main(limit: int = 50):
    """메인 실행"""
    logger.info(f"=== 뉴스 AI 분석 시작 (최대 {limit}건) ===")

    db = SessionLocal()
    try:
        processed = await analyze_unprocessed_news(db, limit=limit)
        logger.info(f"=== 분석 완료: {processed}건 처리 ===")
    except Exception as e:
        logger.error(f"분석 중 오류 발생: {e}")
        raise
    finally:
        db.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="뉴스 AI 분석")
    parser.add_argument("--limit", type=int, default=50, help="처리할 최대 뉴스 수")
    args = parser.parse_args()

    asyncio.run(main(args.limit))
