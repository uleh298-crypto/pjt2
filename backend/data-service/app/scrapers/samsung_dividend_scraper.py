"""
삼성 KODEX ETF 분배금 이력 크롤러

API: GET https://m.samsungfund.com/api/v1/kodex/distribution.do
Params:
  pageNo   : 페이지 번호 (1부터)
  ordrColm : 정렬 컬럼
  period   : 기간 필터 (0=전체, 1=1년)
  ordrSort : 정렬 방향 (DESC)
  srchVal  : 검색어 (ETF 이름 or 코드, 빈값=전체)
"""
import asyncio
import logging
from datetime import date
from typing import Dict, List, Optional

import httpx

logger = logging.getLogger(__name__)

_semaphore = asyncio.Semaphore(3)

_API_URL = "https://m.samsungfund.com/api/v1/kodex/distribution.do"
_HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "Chrome/120.0.0.0 Safari/537.36",
    "Accept": "application/json, text/javascript, */*; q=0.01",
    "Accept-Language": "ko-KR,ko;q=0.9",
    "Referer": "https://m.samsungfund.com/etf/product/distribution.do",
    "X-Requested-With": "XMLHttpRequest",
}


def _parse_date(val: str) -> Optional[date]:
    """YYYYMMDD 또는 YYYY-MM-DD → date"""
    if not val:
        return None
    val = val.strip().replace("-", "").replace(".", "")
    try:
        return date(int(val[:4]), int(val[4:6]), int(val[6:8]))
    except (ValueError, IndexError):
        return None


def _parse_amount(val) -> Optional[float]:
    if val is None:
        return None
    try:
        return float(str(val).replace(",", "").replace("원", "").strip())
    except ValueError:
        return None


def _extract_dividends(records: list) -> List[Dict]:
    """dividList 레코드 → 분배금 이력 파싱"""
    results = []
    for rec in records:
        ex_date = _parse_date(rec.get("gijunYMD") or "")
        pay_date = _parse_date(rec.get("payD") or "")
        amount = _parse_amount(rec.get("dividA"))

        payment = pay_date or ex_date
        if payment is None or amount is None or amount <= 0:
            continue

        results.append({"payment_date": payment, "amount_per_unit": amount})

    return results


async def _fetch_page(client: httpx.AsyncClient, srch_val: str, page_no: int) -> dict:
    params = {
        "pageNo": page_no,
        "ordrColm": "BASE_DATE",
        "period": 0,
        "ordrSort": "DESC",
        "srchVal": srch_val,
        "listCnt": 200,
    }
    resp = await client.get(_API_URL, params=params, headers=_HEADERS)
    resp.raise_for_status()
    return resp.json()


def _extract_list_and_total(data: dict) -> tuple[list, int]:
    records = data.get("dividList") or []
    total = int(data.get("totalCnt") or len(records))
    return records, total


async def fetch_kodex_etf_data(ticker: str, etf_name: str = "") -> Dict:
    """
    KODEX ETF 한 종목의 분배금 이력을 수집합니다.

    Returns:
        {"dividends": [...], "expense_ratio": None}
    """
    srch_val = ticker  # 종목코드로 검색 (이름 검색도 가능)

    async with _semaphore:
        await asyncio.sleep(0.2)
        try:
            async with httpx.AsyncClient(timeout=20.0, follow_redirects=True) as client:
                # 1페이지로 전체 레코드 파악
                data = await _fetch_page(client, srch_val, page_no=1)
                records, total_cnt = _extract_list_and_total(data)

                if not records:
                    logger.debug(f"[{ticker}] 배당 이력 없음")
                    return {"dividends": [], "expense_ratio": None}

                # 첫 응답 필드 확인용 로그 (개발 중)
                logger.debug(f"[{ticker}] 응답 샘플 레코드: {records[0]}")

        except httpx.HTTPStatusError as e:
            logger.warning(f"[{ticker}] 삼성 API HTTP 오류 {e.response.status_code}")
            return {"dividends": [], "expense_ratio": None}
        except Exception as e:
            logger.warning(f"[{ticker}] 삼성 API 요청 실패: {e}")
            return {"dividends": [], "expense_ratio": None}

    dividends = _extract_dividends(records)
    logger.info(f"[{ticker}] 분배금 {len(dividends)}건 수집 (전체 API 레코드 {total_cnt}건)")
    return {"dividends": dividends, "expense_ratio": None}


async def fetch_kodex_etf_data_batch(
    etf_list: List[Dict],  # [{"ticker": str, "name": str}, ...]
) -> Dict[str, Dict]:
    """
    여러 KODEX ETF의 분배금을 일괄 조회합니다.

    Returns:
        {ticker: {"dividends": [...], "expense_ratio": None}}
    """
    tasks = [
        fetch_kodex_etf_data(e["ticker"], etf_name=e.get("name", ""))
        for e in etf_list
    ]
    results_list = await asyncio.gather(*tasks, return_exceptions=True)

    results = {}
    for e, result in zip(etf_list, results_list):
        ticker = e["ticker"]
        if isinstance(result, Exception):
            logger.error(f"[{ticker}] 일괄 조회 예외: {result}")
            results[ticker] = {"dividends": [], "expense_ratio": None}
        else:
            results[ticker] = result

    return results
