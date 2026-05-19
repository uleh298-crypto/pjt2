"""
ETF 상세 메타데이터 일괄 초기화 스크립트
KIS API (FHPST02400000)를 호출하여 활성 ETF의 AUM, NAV, 운용사, 상장일, 배당주기, 섹터를 DB에 저장합니다.

실행:
  python scripts/sync_etf_metadata.py
"""
import asyncio
import logging
import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

from app.database import AsyncSessionLocal
from app.services.etf_service import EtfService


async def main():
    async with AsyncSessionLocal() as db:
        service = EtfService(db)
        await service.sync_etf_metadata()


if __name__ == "__main__":
    asyncio.run(main())
