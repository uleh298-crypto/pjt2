import logging
import asyncio
from datetime import datetime, timezone, timedelta
import json
import redis.asyncio as redis

from app.config import get_settings
from app.services.kis_client import KISClient

logger = logging.getLogger(__name__)
settings = get_settings()

class RedisCacheService:
    """Redis 캐시에 KIS ETF/Stock 정보를 HASH 저장하는 서비스 (Spring Data @RedisHash 호환)"""
    
    def __init__(self):
        self.redis_client = redis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            password=settings.redis_password or None,
            db=settings.redis_db,
            decode_responses=True
        )
        self.kis_client = KISClient()
        self._class_etf = "com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo"
        self._class_stock = "com.whatsyouretf.userservice.domain.company.dto.StockInfo"
        
    async def publish_etf_cache(self, ticker: str, name: str = "", stock_name_map: dict = None):
        basic, constituents = await asyncio.gather(
            self.kis_client.get_etf_basic_info(ticker),
            self.kis_client.get_etf_constituents(ticker),
            return_exceptions=True
        )

        if not basic or isinstance(basic, Exception):
            logger.warning(f"[{ticker}] ETF 기본조회 실패, 캐시 업데이트 스킵")
            return

        out2_list = constituents.get("output2", []) if constituents and not isinstance(constituents, Exception) else []

        # Spring DTO requirements: LocalDateTime format for updatedAt -> "2026-03-23T03:50:23"
        now_dt = datetime.now(timezone(timedelta(hours=9))).strftime("%Y-%m-%dT%H:%M:%S")

        # 1. Update EtfCurrentInfo Cache
        try:
            etf_name = name or basic.get("hts_kor_isnm") or ticker  # DB name 우선, API 응답, 최후엔 ticker
            etf_price = int(basic.get("stck_prpr", 0))
            etf_fluct = int(basic.get("prdy_vrss", 0))  # 변동액 (원)
            nav = float(basic.get("nav", 0.0))
            volume = int(basic.get("acml_vol", 0))  # 거래량 (기본조회에서 가져옴)

            # 전일종가 = 현재가 - 변동액
            etf_prev_price = etf_price - etf_fluct

            # 변동률 = (변동액 / 전일종가) * 100
            etf_daily_return = (etf_fluct / etf_prev_price * 100) if etf_prev_price != 0 else 0.0

            etf_key = f"EtfCurrentInfo:{ticker}"
            etf_fields = {
                "_class": self._class_etf,
                "id": ticker,
                "ticker": ticker,
                "name": etf_name,
                "currentPrice": str(etf_price),
                "previousPrice": str(etf_prev_price),
                "dailyFluctuation": str(etf_fluct),
                "dailyReturn": str(round(etf_daily_return, 2)),
                "nav": str(nav),
                "volume": str(volume),
                "updatedAt": now_dt
            }

            await self.redis_client.sadd("EtfCurrentInfo", ticker)
            await self.redis_client.hset(etf_key, mapping=etf_fields)

        except Exception as e:
            logger.error(f"[{ticker}] ETF Cache 실패: {e}")
            
        # 2. Update StockInfo Cache for all constituents
        for stock in out2_list:
            stock_ticker = stock.get("stck_shrn_iscd")
            if not stock_ticker:
                continue

            try:
                db_name = (stock_name_map or {}).get(stock_ticker, "")
                stock_name = stock.get("hts_kor_isnm") or db_name or stock_ticker
                s_price = int(stock.get("stck_prpr", 0))
                s_fluct = int(stock.get("prdy_vrss", 0))  # 변동액 (원)
                market_cap = int(stock.get("hts_avls", 0))  # 시가총액 (억원)

                # 전일종가 = 현재가 - 변동액
                s_prev_price = s_price - s_fluct

                # 변동률 = (변동액 / 전일종가) * 100
                s_daily_return = (s_fluct / s_prev_price * 100) if s_prev_price != 0 else 0.0

                stock_key = f"StockInfo:{stock_ticker}"
                stock_fields = {
                    "_class": self._class_stock,
                    "ticker": stock_ticker,
                    "stockName": stock_name,
                    "currentPrice": str(s_price),
                    "previousPrice": str(s_prev_price),
                    "dailyFluctuation": str(s_fluct),
                    "dailyReturn": str(round(s_daily_return, 2)),
                    "marketCapitalization": str(market_cap),
                    "updatedAt": now_dt
                }

                await self.redis_client.sadd("StockInfo", stock_ticker)
                await self.redis_client.hset(stock_key, mapping=stock_fields)

            except Exception as e:
                logger.error(f"[{stock_ticker}] Stock Cache 실패: {e}")

        logger.info(f"[{ticker}] Cache Update 완료 (하위 종목 {len(out2_list)}개 포함)")

        # 이번 사이클에서 업데이트된 stock tickers 반환
        return {stock.get("stck_shrn_iscd") for stock in out2_list if stock.get("stck_shrn_iscd")}

    async def publish_stock_cache(self, ticker: str, name: str = ""):
        """개별 주식 현재가 조회 후 Redis StockInfo 업데이트 (Phase 2 - ETF 구성종목 누락분)"""
        data = await self.kis_client.get_stock_price(ticker)
        if not data:
            return

        try:
            now_dt = datetime.now(timezone(timedelta(hours=9))).strftime("%Y-%m-%dT%H:%M:%S")
            # API 응답 hts_kor_isnm 우선, 없으면 DB company_name, 최후 fallback은 ticker
            stock_name = data.get("hts_kor_isnm") or name or ticker
            s_price = int(data.get("stck_prpr", 0))
            s_fluct = int(data.get("prdy_vrss", 0))
            s_prev_price = s_price - s_fluct
            s_daily_return = (s_fluct / s_prev_price * 100) if s_prev_price != 0 else 0.0
            market_cap = int(data.get("hts_avls", 0))

            stock_key = f"StockInfo:{ticker}"
            stock_fields = {
                "_class": self._class_stock,
                "ticker": ticker,
                "stockName": stock_name,
                "currentPrice": str(s_price),
                "previousPrice": str(s_prev_price),
                "dailyFluctuation": str(s_fluct),
                "dailyReturn": str(round(s_daily_return, 2)),
                "marketCapitalization": str(market_cap),
                "updatedAt": now_dt
            }

            await self.redis_client.sadd("StockInfo", ticker)
            await self.redis_client.hset(stock_key, mapping=stock_fields)

        except Exception as e:
            logger.error(f"[{ticker}] Stock Cache (Phase2) 실패: {e}")

    async def close(self):
        await self.redis_client.aclose()
