from anyio.functools import lru_cache

from app.config import get_settings
from app.scrapers.pykrx_client import PykrxClient
from app.scrapers.pykrx_login import KrxSessionManager


@lru_cache
def get_krx_session_manager() -> KrxSessionManager:
    return KrxSessionManager(get_settings())

@lru_cache
def get_pykrx_client() -> PykrxClient:
    return PykrxClient(get_krx_session_manager())