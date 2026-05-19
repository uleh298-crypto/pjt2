import httpx
from bs4 import BeautifulSoup
import logging
import asyncio
from typing import Optional

logger = logging.getLogger(__name__)

# 동시 접속 제한을 위한 세마포어
_semaphore = asyncio.Semaphore(10)

async def crawl_stock_description(ticker: str) -> Optional[str]:
    """네이버 증권 기업정보(coinfo.naver) 페이지에서 기업개요(cmp_comment)를 크롤링합니다."""
    
    # 1. 사용자가 지정한 coinfo.naver URL (이 안의 iframe에 실제 데이터 존재)
    base_url = f"https://finance.naver.com/item/coinfo.naver?code={ticker}"
    
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language": "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
    }
    
    async with _semaphore:
        # Rate limit 방어 (동시 접속 과부하 방지)
        await asyncio.sleep(0.5)
        
        async with httpx.AsyncClient(timeout=15.0) as client:
            try:
                # 2. 실제 데이터가 있는 Wisereport iframe의 URL 조립 (coinfo.naver가 항상 이 곳을 임베드함)
                # 이 iframe 내부 HTML이 UTF-8 인코딩으로 안전하게 한글을 제공합니다.
                iframe_url = f"https://navercomp.wisereport.co.kr/v2/company/c1010001.aspx?cmp_cd={ticker}"
                
                # iframe에 직접 접근 (referer를 coinfo.naver로 주어 보안 정책 우회)
                headers["Referer"] = base_url
                res = await client.get(iframe_url, headers=headers)
                res.raise_for_status()
                
                # Wisereport는 기본적으로 utf-8을 사용하므로, 한글 깨짐 방지
                html_content = res.content.decode('utf-8', 'replace')
                soup = BeautifulSoup(html_content, "html.parser")
                
                # cmp_comment div 또는 하위 텍스트 탐색
                cmp_div = soup.find("div", class_="cmp_comment")
                if cmp_div:
                    # 불필요한 공백과 빈 줄 제거하면서 텍스트 추출
                    text = cmp_div.get_text(separator="\n", strip=True)
                    if text:
                        return text
                        
                return None
            except Exception as e:
                logger.error(f"[{ticker}] 기업개요 크롤링 실패 (coinfo iframe): {e}")
                return None
