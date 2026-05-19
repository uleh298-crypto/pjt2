import httpx
import logging
import asyncio
import os
import time
import collections
from typing import Optional, Dict, Any

from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

# ── 전역 Rate Limiter (슬라이딩 윈도우, 모든 KISClient 인스턴스 공유) ─────────
_RATE_LIMIT_PER_SEC = 18
_rate_window: collections.deque = collections.deque()

# ── 전역 Access Token (모든 인스턴스 공유, asyncio.Lock으로 중복 발급 방지) ───
_global_token: Optional[str] = None
_token_expires_at: float = 0.0          # time.monotonic() 기준 만료 시각
_token_lock: Optional[asyncio.Lock] = None

TOKEN_FILE = "/tmp/kis_token.txt"
_TOKEN_SAFE_MARGIN = 600                # 만료 10분 전에 갱신


def _get_token_lock() -> asyncio.Lock:
    """이벤트루프 생성 이후에 Lock을 lazy-init"""
    global _token_lock
    if _token_lock is None:
        _token_lock = asyncio.Lock()
    return _token_lock


# ── 전역 공유 httpx 클라이언트 (커넥션 풀 재사용, FD 고갈 방지) ──────────────
_shared_http_client: Optional[httpx.AsyncClient] = None

def get_shared_http_client() -> httpx.AsyncClient:
    global _shared_http_client
    if _shared_http_client is None or _shared_http_client.is_closed:
        _shared_http_client = httpx.AsyncClient(
            timeout=15.0,
            limits=httpx.Limits(max_connections=30, max_keepalive_connections=20),
        )
    return _shared_http_client


async def initialize_token() -> None:
    """서버 시작 시 토큰을 미리 발급/로드.
    lifespan 에서 한 번 await 하면 이후 KISClient 인스턴스들이 재발급하지 않는다."""
    client = KISClient()
    token = await client._get_access_token()
    logger.info(f"KIS 토큰 초기화 완료 (앞 20자: {token[:20]}...)")


class KISClient:
    """한국투자증권 Open API 연동 클라이언트"""

    BASE_URL = "https://openapi.koreainvestment.com:9443"

    def __init__(self):
        self.app_key = settings.kis_app_key
        self.app_secret = settings.kis_app_secret

    # ── Rate Limiting ─────────────────────────────────────────────────────────

    async def _acquire_rate_limit(self):
        """슬라이딩 윈도우 방식으로 초당 _RATE_LIMIT_PER_SEC 이하를 보장.
        모든 인스턴스가 모듈 레벨 _rate_window를 공유하므로 인스턴스 수와 무관하다."""
        while True:
            now = time.monotonic()
            # 1초보다 오래된 항목 제거
            while _rate_window and _rate_window[0] < now - 1.0:
                _rate_window.popleft()
            if len(_rate_window) < _RATE_LIMIT_PER_SEC:
                _rate_window.append(now)
                return
            # 가장 오래된 항목이 만료될 때까지 대기
            wait = 1.0 - (now - _rate_window[0]) + 0.001
            await asyncio.sleep(max(wait, 0.001))

    # ── Token Management ──────────────────────────────────────────────────────

    async def _get_access_token(self) -> str:
        """전역 액세스 토큰을 반환한다.

        유효한 토큰이 있으면 즉시 반환.
        없거나 만료됐으면 Lock을 잡고 단 한 번만 발급 (Double-Checked Locking).
        → 17개 ETF를 병렬 처리해도 로그인 API는 최대 1회만 호출됨.
        """
        global _global_token, _token_expires_at

        # 빠른 경로: Lock 없이 확인
        if _global_token and time.monotonic() < _token_expires_at:
            return _global_token

        async with _get_token_lock():
            # Lock 획득 후 재확인
            if _global_token and time.monotonic() < _token_expires_at:
                return _global_token

            # ── 파일 캐시 확인 ──────────────────────────────────────────────
            if os.path.exists(TOKEN_FILE):
                file_age = time.time() - os.path.getmtime(TOKEN_FILE)
                remaining = 86400 - _TOKEN_SAFE_MARGIN - file_age
                if remaining > 0:
                    try:
                        with open(TOKEN_FILE, "r") as f:
                            cached = f.read().strip()
                        if cached:
                            _global_token = cached
                            _token_expires_at = time.monotonic() + remaining
                            logger.info(
                                f"KIS 토큰 파일 캐시 사용 "
                                f"(경과: {file_age:.0f}초, 남은: {remaining:.0f}초)"
                            )
                            return _global_token
                    except Exception as e:
                        logger.warning(f"KIS 토큰 파일 읽기 실패: {e}")

            # ── 신규 발급 ────────────────────────────────────────────────────
            logger.info("KIS API 액세스 토큰 신규 발급 요청 중...")
            url = f"{self.BASE_URL}/oauth2/tokenP"
            payload = {
                "grant_type": "client_credentials",
                "appkey": self.app_key,
                "appsecret": self.app_secret
            }

            http = get_shared_http_client()
            res = await http.post(url, json=payload)
            res.raise_for_status()
            data = res.json()

            token = data.get("access_token")
            if not token:
                raise ValueError(f"KIS 토큰 발급 응답에 access_token 없음: {data}")

            expires_in = int(data.get("expires_in", 86400))
            _global_token = token
            _token_expires_at = time.monotonic() + expires_in - _TOKEN_SAFE_MARGIN

            # 파일 저장
            try:
                with open(TOKEN_FILE, "w") as f:
                    f.write(token)
            except Exception as e:
                logger.warning(f"KIS 토큰 파일 저장 실패: {e}")

            logger.info(f"KIS 토큰 발급 완료 (유효기간: {expires_in}초)")
            return _global_token

    # ── API Methods ───────────────────────────────────────────────────────────

    async def get_etf_constituents(self, ticker: str) -> Optional[Dict[str, Any]]:
        """FHKST121600C0: ETF 구성종목시세
        output1 = ETF 본체 가격/기본정보,  output2 = 구성종목 리스트 (최대 30개)
        """
        if not self.app_key or not self.app_secret:
            return None

        token = await self._get_access_token()
        await self._acquire_rate_limit()

        url = f"{self.BASE_URL}/uapi/etfetn/v1/quotations/inquire-component-stock-price"
        headers = {
            "content-type": "application/json; charset=utf-8",
            "authorization": f"Bearer {token}",
            "appkey": self.app_key,
            "appsecret": self.app_secret,
            "tr_id": "FHKST121600C0",
            "custtype": "P"
        }
        params = {
            "fid_cond_mrkt_div_code": "J",
            "fid_input_iscd": ticker,
            "fid_cond_scr_div_code": 11216
        }

        client = get_shared_http_client()
        try:
            res = await client.get(url, headers=headers, params=params)
            res.raise_for_status()
            data = res.json()
            if data.get("rt_cd") != "0":
                logger.error(f"[{ticker}] FHKST121600C0 에러: {data.get('msg1')}")
                return None
            return {
                "output1": data.get("output1", {}),
                "output2": data.get("output2", [])
            }
        except Exception as e:
            logger.error(f"[{ticker}] FHKST121600C0 호출 실패: {e}")
            return None

    async def get_etf_basic_info(self, ticker: str) -> Optional[Dict[str, Any]]:
        """FHPST02400000: ETF 현재가/기본정보
        acml_vol(거래량), nav, stck_prpr(현재가), prdy_vrss(전일대비) 등 포함
        """
        if not self.app_key or not self.app_secret:
            return None

        token = await self._get_access_token()
        await self._acquire_rate_limit()

        url = f"{self.BASE_URL}/uapi/domestic-stock/v1/quotations/inquire-price"
        headers = {
            "content-type": "application/json; charset=utf-8",
            "authorization": f"Bearer {token}",
            "appkey": self.app_key,
            "appsecret": self.app_secret,
            "tr_id": "FHPST02400000",
            "custtype": "P"
        }
        params = {
            "FID_COND_MRKT_DIV_CODE": "J",
            "FID_INPUT_ISCD": ticker
        }

        client = get_shared_http_client()
        try:
            res = await client.get(url, headers=headers, params=params)
            res.raise_for_status()
            data = res.json()
            if data.get("rt_cd") != "0":
                logger.error(f"[{ticker}] FHPST02400000 에러: {data.get('msg1')}")
                return None
            return data.get("output", {})
        except Exception as e:
            logger.error(f"[{ticker}] FHPST02400000 호출 실패: {e}")
            return None

    async def get_stock_price(self, ticker: str) -> Optional[Dict[str, Any]]:
        """FHKST01010100: 주식 현재가 조회
        stck_prpr(현재가), prdy_vrss(전일대비), hts_kor_isnm(종목명), hts_avls(시가총액) 등
        """
        if not self.app_key or not self.app_secret:
            return None

        token = await self._get_access_token()
        await self._acquire_rate_limit()

        url = f"{self.BASE_URL}/uapi/domestic-stock/v1/quotations/inquire-price"
        headers = {
            "content-type": "application/json; charset=utf-8",
            "authorization": f"Bearer {token}",
            "appkey": self.app_key,
            "appsecret": self.app_secret,
            "tr_id": "FHKST01010100",
            "custtype": "P"
        }
        params = {
            "FID_COND_MRKT_DIV_CODE": "J",
            "FID_INPUT_ISCD": ticker
        }

        client = get_shared_http_client()
        try:
            res = await client.get(url, headers=headers, params=params)
            res.raise_for_status()
            data = res.json()
            if data.get("rt_cd") != "0":
                logger.error(f"[{ticker}] FHKST01010100 에러: {data.get('msg1')}")
                return None
            return data.get("output", {})
        except Exception as e:
            logger.error(f"[{ticker}] FHKST01010100 호출 실패: {e}")
            return None
