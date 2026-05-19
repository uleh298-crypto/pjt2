import asyncio
import logging
from typing import Optional, Dict

import httpx
from app.config import get_settings

logger = logging.getLogger(__name__)

class DataPortalClient:
    STOCK_INFO_URL = "https://apis.data.go.kr/1160100/service/GetKrxListedInfoService/getItemInfo"
    CORP_OUTLINE_URL = "https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2/getCorpOutline_V2"

    def __init__(self):
        self.service_key = get_settings().data_portal_company_service_key

    async def get_stock_item_info(self, ticker: str) -> Optional[Dict]:
        """
        Stock info by ticker
        returns:
        {
            "srtnCd": ticker,
            "mrktCtg": market_type,
            "corpNm": company_name,
            "crno": corp_number
        }
        """
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                response = await client.get(self.STOCK_INFO_URL, params={
                    "ServiceKey": self.service_key,
                    "likeSrtnCd": ticker,
                    "resultType": "json",
                    "numOfRows": 1,
                    "pageNo": 1
                })
                response.raise_for_status()
                data = response.json()
                items = data.get("response", {}).get("body", {}).get("items", {}).get("item", [])
                if items:
                    return items[0]
            except Exception as e:
                logger.error(f"Failed to fetch get_stock_item_info for {ticker}: {e}")
        return None

    async def get_corp_outline(self, corp_number: str) -> Optional[Dict]:
        """
        Corp outline by corp_number (crno)
        returns:
        {
            "sicNm": industry_name,
            "enpRprFnm": ceo_name,
            "enpBsadr": address/region,
            "enpHmpgUrl": homepage,
            "enpMainBizNm": description
        }
        """
        async with httpx.AsyncClient(timeout=10.0) as client:
            try:
                response = await client.get(self.CORP_OUTLINE_URL, params={
                    "ServiceKey": self.service_key,
                    "crno": corp_number,
                    "resultType": "json",
                    "numOfRows": 1,
                    "pageNo": 1
                })
                response.raise_for_status()
                data = response.json()
                items = data.get("response", {}).get("body", {}).get("items", {}).get("item", [])
                if items:
                    return items[0]
            except Exception as e:
                logger.error(f"Failed to fetch get_corp_outline for {corp_number}: {e}")
        return None
