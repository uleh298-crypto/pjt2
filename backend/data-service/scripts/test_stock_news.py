"""썸네일 추출 테스트"""
import asyncio
import re
import httpx
from bs4 import BeautifulSoup


async def test_thumbnail():
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    }

    async with httpx.AsyncClient(headers=headers, timeout=30.0) as client:
        # 삼성전자 뉴스
        url = "https://finance.naver.com/item/main.naver?code=005930"
        resp = await client.get(url)
        soup = BeautifulSoup(resp.text, "lxml")

        links = [a.get("href") for a in soup.select("a[href*='news_read.naver']")][:2]

        for link in links:
            full_url = "https://finance.naver.com" + link
            resp = await client.get(full_url)
            match = re.search(r"top\.location\.href='([^']+)'", resp.text)
            if not match:
                continue

            real_url = match.group(1)
            resp = await client.get(real_url)
            article = BeautifulSoup(resp.text, "lxml")

            # 제목
            title_el = article.select_one("#title_area")
            title = title_el.get_text(strip=True) if title_el else ""

            # 썸네일 (og:image)
            og_image = article.select_one("meta[property='og:image']")
            thumb = og_image.get("content") if og_image else None

            print(f"제목: {title[:50]}...")
            print(f"썸네일: {thumb}")
            print()


if __name__ == "__main__":
    asyncio.run(test_thumbnail())
