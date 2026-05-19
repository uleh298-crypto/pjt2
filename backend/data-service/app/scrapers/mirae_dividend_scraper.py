"""
미래에셋 Tiger ETF 사이트 분배금 이력 + 총보수 크롤러

분배금 데이터는 팝업 버튼 클릭 시 AJAX로 로드됨.
절차:
  GET index.do?ksdFund=... → 세션 쿠키 + 총보수 파싱
  POST refDivAjax.ajax → 분배금 테이블 HTML 수신 → tbody.listArea 파싱

컬럼 순서: 지급기준일(0) | 실제지급일(1) | 분배금액(원)(2) | 주당 과세 표준액(원)(3)
"""
import asyncio
import logging
import re
from datetime import date
from typing import Dict, List, Optional

import httpx
from bs4 import BeautifulSoup

logger = logging.getLogger(__name__)

# 동시 요청 제한 (미래에셋 서버 부하 방지)
_semaphore = asyncio.Semaphore(3)

_AJAX_URL = "https://investments.miraeasset.com/tigeretf/ko/product/search/detail/refDivAjax.ajax"
_REFERER = "https://investments.miraeasset.com/tigeretf/ko/product/search/detail/index.do"

_AJAX_HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "Chrome/120.0.0.0 Safari/537.36",
    "Accept": "text/html, */*; q=0.01",
    "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8",
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With": "XMLHttpRequest",
    "Referer": _REFERER,
}


def _parse_expense_ratio(html: str) -> Optional[float]:
    """
    ETF 메인 페이지 HTML에서 총보수율(%)을 파싱합니다.

    <div class="c-card" data-type="definition">
      <div class="c-card-header">총보수</div>
      <div class="c-card-content"><p>연 0.05% </p>...</div>
    </div>
    """
    soup = BeautifulSoup(html, "html.parser")
    for card in soup.select("div.c-card[data-type='definition']"):
        header = card.find("div", class_="c-card-header")
        if header and "총보수" in header.get_text():
            content = card.find("p")
            if content:
                text = content.get_text(strip=True)
                m = re.search(r"([\d.]+)\s*%", text)
                if m:
                    return float(m.group(1))
    return None


def ticker_to_ksd_fund(ticker: str) -> str:
    """
    ETF 종목코드(6자리) → KSD 펀드코드(ISIN) 변환
    예: '102110' → 'KR7102110004'
    """
    return f"KR7{ticker.zfill(6)}004"


def _parse_dividend_table(html: str) -> List[Dict]:
    """
    AJAX 응답 HTML에서 분배금 테이블 행을 파싱합니다.

    실제 컬럼 순서: 지급기준일(0) | 실제지급일(1) | 분배금액(2) | 과세표준액(3)
    → payment_date = col[1] (실제지급일), amount_per_unit = col[2] (분배금액)
    """
    soup = BeautifulSoup(html, "html.parser")

    # AJAX 응답은 <tr> 만 반환 (tbody 래퍼 없음)
    # tbody가 있으면 그 안에서, 없으면 soup 최상위에서 tr 수집
    tbody = (
        soup.find("tbody", class_="listArea")
        or soup.select_one("div.pop-table-wrap tbody")
        or soup.find("tbody")
    )
    rows = tbody.find_all("tr") if tbody else soup.find_all("tr")

    if not rows:
        logger.debug("분배금 테이블 행을 찾을 수 없음")
        return []

    results = []
    for row in rows:
        cols = [td.get_text(strip=True) for td in row.find_all("td")]
        if len(cols) < 3:
            continue

        # 빈 행 / 데이터 없음 메시지 건너뜀
        if not cols[0] or "데이터" in cols[0] or "없" in cols[0]:
            continue

        try:
            # 실제지급일(col[1]): YYYY-MM-DD 또는 YYYY.MM.DD
            date_str = cols[1].replace(".", "-").strip()
            payment_date = date.fromisoformat(date_str)

            # 분배금액(col[2]): 쉼표/원 제거 후 float
            amount_str = cols[2].replace(",", "").replace("원", "").strip()
            amount = float(amount_str)

            results.append({
                "payment_date": payment_date,
                "amount_per_unit": amount,
            })
        except (ValueError, IndexError) as e:
            logger.debug(f"분배금 행 파싱 실패 (cols={cols}): {e}")
            continue

    return results


async def fetch_tiger_etf_data(ticker: str, isin: str = "", etf_name: str = "") -> Dict:
    """
    미래에셋 Tiger ETF 분배금 이력 + 총보수를 크롤링합니다.

    Returns:
        {"dividends": [...], "expense_ratio": float | None}
    """
    ksd_fund = isin or ticker_to_ksd_fund(ticker)

    form_data = {
        "ksdFund": ksd_fund,
        "jongName": etf_name,
        "pageIndex": "1",
        "firstIndex": "0",
        "listCnt": "200",
    }

    async with _semaphore:
        await asyncio.sleep(0.3)
        try:
            async with httpx.AsyncClient(timeout=20.0, follow_redirects=True) as client:
                # 메인 페이지: 세션 쿠키 + 총보수 파싱
                main_resp = await client.get(
                    _REFERER,
                    params={"ksdFund": ksd_fund},
                    headers={"User-Agent": _AJAX_HEADERS["User-Agent"]},
                )
                expense_ratio = _parse_expense_ratio(main_resp.text)

                resp = await client.post(_AJAX_URL, data=form_data, headers=_AJAX_HEADERS)
                resp.raise_for_status()
                if len(resp.text) < 50:
                    logger.warning(f"[{ticker}] 분배금 응답이 짧음 ({len(resp.text)}자): {resp.text!r}")
        except httpx.HTTPStatusError as e:
            logger.warning(f"[{ticker}] 미래에셋 HTTP 오류 {e.response.status_code}")
            return {"dividends": [], "expense_ratio": None}
        except Exception as e:
            logger.warning(f"[{ticker}] 미래에셋 요청 실패: {e}")
            return {"dividends": [], "expense_ratio": None}

    dividends = _parse_dividend_table(resp.text)
    logger.info(f"[{ticker}] 분배금 {len(dividends)}건, 총보수 {expense_ratio}%")
    return {"dividends": dividends, "expense_ratio": expense_ratio}


async def fetch_tiger_etf_data_batch(
    etf_list: List[Dict],  # [{"ticker": str, "isin": str, "name": str}, ...]
) -> Dict[str, Dict]:
    """
    여러 Tiger ETF의 분배금 + 총보수를 일괄 조회합니다.

    Returns:
        {ticker: {"dividends": [...], "expense_ratio": float | None}}
    """
    tasks = [
        fetch_tiger_etf_data(e["ticker"], isin=e.get("isin", ""), etf_name=e.get("name", ""))
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
