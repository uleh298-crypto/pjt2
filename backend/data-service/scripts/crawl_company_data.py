"""네이버 증권 회사 정보 크롤러

수집 데이터:
1. 기업개요 (description)
2. 테마 정보 (industry classification용)
3. WICS 업종 (group_code용)
"""
import asyncio
import csv
import json
import re
from pathlib import Path
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass, asdict

import httpx
from bs4 import BeautifulSoup


@dataclass
class CompanyData:
    """회사 데이터"""
    stock_code: str
    stock_name: str
    description: Optional[str] = None
    wics: Optional[str] = None
    themes: List[str] = None

    def __post_init__(self):
        if self.themes is None:
            self.themes = []


@dataclass
class ThemeData:
    """테마 데이터"""
    theme_no: str
    theme_name: str
    stocks: List[Tuple[str, str]]  # (stock_code, stock_name)


class NaverFinanceCrawler:
    """네이버 증권 크롤러"""

    BASE_URL = "https://finance.naver.com"
    WISEREPORT_URL = "https://navercomp.wisereport.co.kr"
    HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    def __init__(self):
        self.client: Optional[httpx.AsyncClient] = None
        self.themes: Dict[str, ThemeData] = {}
        self.companies: Dict[str, CompanyData] = {}

    async def __aenter__(self):
        self.client = httpx.AsyncClient(headers=self.HEADERS, timeout=30)
        return self

    async def __aexit__(self, *args):
        if self.client:
            await self.client.aclose()

    async def crawl_all_themes(self) -> Dict[str, ThemeData]:
        """모든 테마 크롤링"""
        print("테마 목록 수집 중...")

        # 1. 테마 목록 수집
        theme_list = {}
        for page in range(1, 15):
            url = f"{self.BASE_URL}/sise/theme.naver?page={page}"
            resp = await self.client.get(url)
            soup = BeautifulSoup(resp.text, "lxml")

            for a in soup.select('a[href*="sise_group_detail"]'):
                name = a.get_text(strip=True)
                href = a.get("href", "")
                match = re.search(r'no=(\d+)', href)
                if match and name:
                    theme_no = match.group(1)
                    if theme_no not in theme_list:
                        theme_list[theme_no] = name

            await asyncio.sleep(0.2)

        print(f"  테마 수: {len(theme_list)}개")

        # 2. 각 테마별 종목 수집
        print("테마별 종목 수집 중...")
        for i, (theme_no, theme_name) in enumerate(theme_list.items()):
            stocks = await self._get_theme_stocks(theme_no)
            self.themes[theme_no] = ThemeData(
                theme_no=theme_no,
                theme_name=theme_name,
                stocks=stocks
            )

            if (i + 1) % 20 == 0:
                print(f"  진행: {i + 1}/{len(theme_list)}")

            await asyncio.sleep(0.3)

        return self.themes

    async def _get_theme_stocks(self, theme_no: str) -> List[Tuple[str, str]]:
        """테마별 종목 리스트 추출"""
        url = f"{self.BASE_URL}/sise/sise_group_detail.naver?type=theme&no={theme_no}"
        resp = await self.client.get(url)
        soup = BeautifulSoup(resp.text, "lxml")

        stocks = []
        for tr in soup.select("table.type_5 tbody tr"):
            a = tr.select_one('td a[href*="main.naver"]')
            if a:
                stock_name = a.get_text(strip=True)
                href = a.get("href", "")
                match = re.search(r'code=(\d+)', href)
                if match:
                    stocks.append((match.group(1), stock_name))

        return stocks

    async def crawl_company_info(self, stock_codes: List[str]) -> Dict[str, CompanyData]:
        """회사 정보 크롤링 (기업개요 + WICS)"""
        print(f"회사 정보 수집 중... ({len(stock_codes)}개)")

        for i, code in enumerate(stock_codes):
            if code in self.companies:
                continue

            # 기업개요
            description = await self._get_company_description(code)

            # WICS
            wics = await self._get_wics(code)

            self.companies[code] = CompanyData(
                stock_code=code,
                stock_name="",  # 나중에 채움
                description=description,
                wics=wics
            )

            if (i + 1) % 50 == 0:
                print(f"  진행: {i + 1}/{len(stock_codes)}")

            await asyncio.sleep(0.3)

        return self.companies

    async def _get_company_description(self, stock_code: str) -> Optional[str]:
        """기업개요 추출"""
        url = f"{self.BASE_URL}/item/main.naver?code={stock_code}"
        try:
            resp = await self.client.get(url)
            soup = BeautifulSoup(resp.text, "lxml")

            summary = soup.select_one(".summary_info")
            if summary:
                text = summary.get_text(strip=True)
                # '기업개요' 제목 제거, '출처' 이후 제거
                text = text.replace("기업개요", "").strip()
                if "출처" in text:
                    text = text[:text.index("출처")].strip()
                return text if text else None
        except Exception:
            pass
        return None

    async def _get_wics(self, stock_code: str) -> Optional[str]:
        """WICS 업종 추출"""
        url = f"{self.WISEREPORT_URL}/v2/company/c1010001.aspx?cmp_cd={stock_code}"
        try:
            resp = await self.client.get(url)
            match = re.search(r'WICS\s*[：:]\s*([가-힣a-zA-Z0-9/]+)', resp.text)
            if match:
                return match.group(1)
        except Exception:
            pass
        return None

    def build_company_theme_mapping(self) -> Dict[str, List[str]]:
        """회사 → 테마 매핑 생성"""
        mapping = {}

        for theme in self.themes.values():
            for stock_code, stock_name in theme.stocks:
                if stock_code not in mapping:
                    mapping[stock_code] = []
                    # 회사 데이터에 이름 추가
                    if stock_code in self.companies:
                        self.companies[stock_code].stock_name = stock_name

                mapping[stock_code].append(theme.theme_name)

                # 회사 데이터에 테마 추가
                if stock_code in self.companies:
                    if theme.theme_name not in self.companies[stock_code].themes:
                        self.companies[stock_code].themes.append(theme.theme_name)

        return mapping

    def save_results(self, output_dir: str):
        """결과 저장"""
        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)

        # 1. 테마 목록 저장
        themes_file = output_path / "naver_themes.json"
        themes_data = {
            no: {
                "name": t.theme_name,
                "stock_count": len(t.stocks),
                "stocks": [{"code": c, "name": n} for c, n in t.stocks]
            }
            for no, t in self.themes.items()
        }
        with open(themes_file, "w", encoding="utf-8") as f:
            json.dump(themes_data, f, ensure_ascii=False, indent=2)
        print(f"저장: {themes_file}")

        # 2. 회사 정보 저장
        companies_file = output_path / "company_data.json"
        companies_data = {
            code: asdict(c) for code, c in self.companies.items()
        }
        with open(companies_file, "w", encoding="utf-8") as f:
            json.dump(companies_data, f, ensure_ascii=False, indent=2)
        print(f"저장: {companies_file}")

        # 3. 회사-테마 매핑 CSV
        mapping_file = output_path / "company_themes.csv"
        with open(mapping_file, "w", encoding="utf-8-sig", newline="") as f:
            writer = csv.writer(f)
            writer.writerow(["stock_code", "stock_name", "wics", "themes", "description"])
            for code, c in self.companies.items():
                writer.writerow([
                    code,
                    c.stock_name,
                    c.wics or "",
                    "|".join(c.themes),
                    (c.description or "")[:200]
                ])
        print(f"저장: {mapping_file}")


async def main():
    """메인 실행"""
    output_dir = "data/crawled"

    async with NaverFinanceCrawler() as crawler:
        # 1. 테마 크롤링
        await crawler.crawl_all_themes()

        # 2. 테마에서 고유 종목 추출
        all_stock_codes = set()
        for theme in crawler.themes.values():
            for code, _ in theme.stocks:
                all_stock_codes.add(code)

        print(f"\n총 고유 종목: {len(all_stock_codes)}개")

        # 3. 회사 정보 크롤링
        await crawler.crawl_company_info(list(all_stock_codes))

        # 4. 매핑 생성
        crawler.build_company_theme_mapping()

        # 5. 결과 저장
        crawler.save_results(output_dir)

        print("\n완료!")


if __name__ == "__main__":
    asyncio.run(main())
