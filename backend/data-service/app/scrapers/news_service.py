"""뉴스 수집 서비스

네이버 증권 종목뉴스 크롤링을 통합 관리하는 서비스
- ETF 구성종목 기반 뉴스 수집
- 키워드 기반 수집 (종목명/종목코드)
"""
import logging
from typing import List, Dict, Any, Optional

from sqlalchemy.orm import Session
from sqlalchemy import text

from app.scrapers.stock_news_scraper import StockNewsScraper
from app.models.company import CompanyInfo

logger = logging.getLogger(__name__)


class NewsCollectionService:
    """
    뉴스 수집 통합 서비스

    main.py API 엔드포인트에서 사용:
    - collect_all: 기본 수집 (상위 ETF 구성종목)
    - collect_full: 전체 수집 (모든 활성 종목)
    - collect_by_keywords: 키워드 기반 수집
    """

    def __init__(self, db: Session):
        self.db = db
        self.scraper: Optional[StockNewsScraper] = None

    async def _ensure_scraper(self):
        """스크래퍼 초기화"""
        if not self.scraper:
            self.scraper = StockNewsScraper(self.db)
            await self.scraper.__aenter__()

    async def close(self):
        """리소스 정리"""
        if self.scraper:
            await self.scraper.__aexit__(None, None, None)
            self.scraper = None

    async def collect_all(self, enrich_content: bool = True) -> Dict[str, int]:
        """
        기본 뉴스 수집 (상위 50개 ETF 구성종목)

        Args:
            enrich_content: 본문 수집 여부 (네이버 증권은 항상 본문 포함)

        Returns:
            {google_count, naver_count, content_enriched, total}
        """
        await self._ensure_scraper()

        # 상위 50개 ETF 구성종목 조회
        query = text("""
            SELECT DISTINCT c.stock_code
            FROM company_info c
            JOIN etf_stock_composition esc ON esc.company_id = c.id
            WHERE c.is_active = true
              AND c.stock_code IS NOT NULL
              AND esc.etf_id IN (
                  SELECT id FROM etf
                  WHERE is_active = true
                  ORDER BY aum DESC NULLS LAST
                  LIMIT 50
              )
            LIMIT 100
        """)

        result = self.db.execute(query)
        stocks = [row[0] for row in result.fetchall()]

        return await self._scrape_stocks(stocks, max_articles=5)

    async def collect_full(self) -> Dict[str, int]:
        """
        전체 뉴스 수집 (모든 활성 종목)

        Returns:
            {google_count, naver_count, content_enriched, total}
        """
        await self._ensure_scraper()

        # 모든 활성 종목 + ETF 관련 종목
        query = text("""
            SELECT DISTINCT c.stock_code
            FROM company_info c
            JOIN etf_stock_composition esc ON esc.company_id = c.id
            WHERE c.is_active = true
              AND c.stock_code IS NOT NULL
            LIMIT 500
        """)

        result = self.db.execute(query)
        stocks = [row[0] for row in result.fetchall()]

        return await self._scrape_stocks(stocks, max_articles=10)

    async def collect_by_keywords(
        self,
        keywords: List[str],
        enrich_content: bool = True
    ) -> Dict[str, int]:
        """
        키워드 기반 뉴스 수집

        키워드가 종목코드(6자리 숫자)면 해당 종목 뉴스 수집
        키워드가 종목명이면 회사 검색 후 수집

        Args:
            keywords: 종목코드 또는 종목명 리스트
            enrich_content: 본문 수집 여부

        Returns:
            {google_count, naver_count, content_enriched, total}
        """
        await self._ensure_scraper()

        stock_codes = []

        for keyword in keywords:
            keyword = keyword.strip()

            # 6자리 숫자면 종목코드로 간주
            if keyword.isdigit() and len(keyword) == 6:
                stock_codes.append(keyword)
            else:
                # 종목명으로 검색
                company = self.db.query(CompanyInfo).filter(
                    CompanyInfo.stock_name.ilike(f"%{keyword}%"),
                    CompanyInfo.is_active == True
                ).first()

                if company and company.stock_code:
                    stock_codes.append(company.stock_code)
                    logger.info(f"키워드 '{keyword}' -> 종목코드 '{company.stock_code}'")

        if not stock_codes:
            logger.warning(f"유효한 종목 없음: {keywords}")
            return {"google_count": 0, "naver_count": 0, "content_enriched": 0, "total": 0}

        return await self._scrape_stocks(stock_codes, max_articles=10)

    async def _scrape_stocks(
        self,
        stock_codes: List[str],
        max_articles: int = 5
    ) -> Dict[str, int]:
        """
        종목 목록 뉴스 크롤링

        Args:
            stock_codes: 종목코드 리스트
            max_articles: 종목당 최대 기사 수

        Returns:
            {google_count, naver_count, content_enriched, total}
        """
        total_stats = {"total": 0, "new": 0, "mapped": 0}

        for stock_code in stock_codes:
            try:
                stats = await self.scraper.scrape_stock_news(
                    stock_code=stock_code,
                    max_articles=max_articles
                )
                total_stats["total"] += stats["total"]
                total_stats["new"] += stats["new"]
                total_stats["mapped"] += stats["mapped"]
            except Exception as e:
                logger.error(f"종목 크롤링 실패 [{stock_code}]: {e}")
                continue

        # main.py ScrapeResult 형식에 맞게 변환
        return {
            "google_count": 0,  # Google 크롤링 미사용
            "naver_count": total_stats["new"],  # 네이버에서 신규 수집
            "content_enriched": total_stats["new"],  # 네이버 증권은 항상 본문 포함
            "total": total_stats["total"]
        }
