"""
실제 데이터 삽입 스크립트

사용법:
    cd backend/data-service
    python -m scripts.insert_real_data
"""
import sys
import csv
import json
from pathlib import Path
from datetime import date

sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import text
from app.database import SessionLocal, engine

# 데이터 파일 경로
DATA_DIR = Path(__file__).parent.parent.parent.parent / "docs" / "sql" / "data"
ETF_CSV = DATA_DIR / "processed_etf_data.csv"
COMPANY_CSV = DATA_DIR / "company_infos.csv"
STOCK_CSV = DATA_DIR / "stock_info.csv"
COMPOSITION_DIR = DATA_DIR / "pdf_datas"

# 분류 데이터
CRAWLED_DIR = Path(__file__).parent.parent / "data" / "crawled"
CLASSIFICATION_JSON = CRAWLED_DIR / "company_classification.json"


def clean_tables(db):
    """기존 샘플 데이터 삭제"""
    print("기존 데이터 삭제 중...")
    db.execute(text("DELETE FROM etf_stock_composition"))
    db.execute(text("DELETE FROM etf_compositions"))
    db.execute(text("DELETE FROM etf_sector_cluster"))
    db.execute(text("DELETE FROM stock WHERE id > 0"))
    db.execute(text("DELETE FROM etf WHERE id > 0"))
    db.execute(text("DELETE FROM company_info WHERE id > 0"))
    db.commit()
    print("기존 데이터 삭제 완료")


def insert_etf_data(db):
    """ETF 데이터 삽입 (국내주식 KODEX, TIGER만)"""
    print("\n=== ETF 데이터 삽입 ===")

    # 국내주식 ETF만 필터링 (채권형 제외)
    domestic_strategies = ['시장 대표', '테마형', '배당형']

    etfs = []
    with open(ETF_CSV, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # KODEX, TIGER만 + 국내주식형만
            if row['asset_manager'] not in ['KODEX', 'TIGER']:
                continue
            if row['strategy_type'] not in domestic_strategies:
                continue

            etfs.append({
                'stock_code': row['ticker'],
                'name': f"{row['asset_manager']} {row['name']}",
                'asset_manager': row['asset_manager'],
                'strategy_type': row['strategy_type'],
                'sector': row['sector'] if row['sector'] else None,
                'category': '국내주식형',
                'asset_class': 'EQUITY',
                'is_active': True
            })

    # 삽입
    for etf in etfs:
        db.execute(text("""
            INSERT INTO etf (stock_code, name, asset_manager, strategy_type, sector, category, asset_class, is_active)
            VALUES (:stock_code, :name, :asset_manager, :strategy_type, :sector, :category, :asset_class, :is_active)
            ON CONFLICT (stock_code) DO UPDATE SET
                name = EXCLUDED.name,
                asset_manager = EXCLUDED.asset_manager,
                strategy_type = EXCLUDED.strategy_type,
                sector = EXCLUDED.sector
        """), etf)

    db.commit()
    print(f"ETF {len(etfs)}건 삽입 완료")
    return etfs


def insert_company_data(db):
    """회사 데이터 삽입 (ETF 구성종목에 포함된 회사만)"""
    print("\n=== 회사 데이터 삽입 ===")

    # stock_info.csv 로드 (ticker -> corp_code 매핑)
    stock_info = {}
    with open(STOCK_CSV, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # A005930 -> 005930
            ticker = row['ticker'].replace('A', '')
            stock_info[ticker] = {
                'corp_code': row['corp_code'],
                'corp_name': row['corp_name'],
                'market_type': row['market_type']
            }

    # company_infos.csv 로드 (corp_code -> 상세정보)
    company_details = {}
    with open(COMPANY_CSV, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for row in reader:
            company_details[row['crno']] = {
                'ceo_name': row['enp_rpr_fnm'],
                'homepage': row['homepage'],
                'corp_name': row['corp_name']
            }

    # ETF 구성종목에서 사용되는 종목 코드 수집
    used_tickers = set()
    for csv_file in COMPOSITION_DIR.glob("*.csv"):
        with open(csv_file, 'r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            for row in reader:
                ticker = row.get('티커', '').strip()
                if ticker and ticker.isdigit():
                    used_tickers.add(ticker.zfill(6))

    print(f"ETF 구성종목에 사용된 종목: {len(used_tickers)}개")

    # 회사 데이터 삽입
    inserted = 0
    for ticker in used_tickers:
        info = stock_info.get(ticker)
        if not info:
            continue

        details = company_details.get(info['corp_code'], {})

        db.execute(text("""
            INSERT INTO company_info (stock_code, stock_name, market_type, ceo_name, homepage, is_active)
            VALUES (:stock_code, :stock_name, :market_type, :ceo_name, :homepage, true)
            ON CONFLICT (stock_code) DO UPDATE SET
                stock_name = EXCLUDED.stock_name,
                market_type = EXCLUDED.market_type,
                ceo_name = EXCLUDED.ceo_name,
                homepage = EXCLUDED.homepage
        """), {
            'stock_code': ticker,
            'stock_name': details.get('corp_name') or info['corp_name'],
            'market_type': info['market_type'],
            'ceo_name': details.get('ceo_name'),
            'homepage': details.get('homepage')
        })
        inserted += 1

    db.commit()
    print(f"회사 {inserted}건 삽입 완료")


def update_industry_groups(db):
    """company_classification.json에서 industry_group 업데이트"""
    print("\n=== Industry Group 업데이트 ===")

    if not CLASSIFICATION_JSON.exists():
        print(f"분류 파일 없음: {CLASSIFICATION_JSON}")
        return

    with open(CLASSIFICATION_JSON, 'r', encoding='utf-8') as f:
        classifications = json.load(f)

    updated = 0
    for ticker, data in classifications.items():
        industry_group = data.get('industry_group')

        if not industry_group:
            continue

        # 6자리로 패딩
        ticker = ticker.zfill(6)

        db.execute(text("""
            UPDATE company_info
            SET industry_group = :industry_group
            WHERE stock_code = :ticker
        """), {
            'industry_group': industry_group,
            'ticker': ticker
        })
        updated += 1

    db.commit()
    print(f"Industry group {updated}건 업데이트 완료")


def insert_stock_data(db):
    """stock 테이블 삽입 (company_info 기반)"""
    print("\n=== Stock 데이터 삽입 ===")

    # company_info에서 stock 테이블로 복사
    db.execute(text("""
        INSERT INTO stock (company_id, ticker, market_type, is_active)
        SELECT c.id, c.stock_code, c.market_type, true
        FROM company_info c
        WHERE c.stock_code IS NOT NULL
        ON CONFLICT (ticker) DO NOTHING
    """))

    # ticker에 UNIQUE 제약이 없으면 직접 삽입
    result = db.execute(text("SELECT COUNT(*) FROM stock"))
    count = result.fetchone()[0]

    if count == 0:
        result = db.execute(text("SELECT id, stock_code, market_type FROM company_info WHERE stock_code IS NOT NULL"))
        for row in result.fetchall():
            db.execute(text("""
                INSERT INTO stock (company_id, ticker, market_type, is_active)
                VALUES (:company_id, :ticker, :market_type, true)
            """), {'company_id': row[0], 'ticker': row[1], 'market_type': row[2]})

    db.commit()
    result = db.execute(text("SELECT COUNT(*) FROM stock"))
    print(f"Stock {result.fetchone()[0]}건 삽입 완료")


def insert_etf_compositions(db):
    """ETF 구성종목 삽입"""
    print("\n=== ETF 구성종목 삽입 ===")

    # ETF stock_code -> id 매핑
    result = db.execute(text("SELECT id, stock_code FROM etf"))
    etf_map = {row[1]: row[0] for row in result.fetchall()}

    # stock ticker -> id 매핑
    result = db.execute(text("SELECT id, ticker FROM stock"))
    stock_map = {row[1]: row[0] for row in result.fetchall()}

    today = date.today()
    total_inserted = 0

    for csv_file in COMPOSITION_DIR.glob("*.csv"):
        etf_code = csv_file.stem  # 091160
        etf_id = etf_map.get(etf_code)

        if not etf_id:
            continue

        with open(csv_file, 'r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            for row in reader:
                ticker = row.get('티커', '').strip()
                if not ticker or not ticker.isdigit():
                    continue

                ticker = ticker.zfill(6)
                stock_id = stock_map.get(ticker)

                if not stock_id:
                    continue

                weight = float(row.get('비중', 0))

                db.execute(text("""
                    INSERT INTO etf_stock_composition
                    (etf_id, stock_id, weight_pct, base_date)
                    VALUES (:etf_id, :stock_id, :weight, :base_date)
                """), {
                    'etf_id': etf_id,
                    'stock_id': stock_id,
                    'weight': weight,
                    'base_date': today
                })
                total_inserted += 1

    db.commit()
    print(f"ETF 구성종목 {total_inserted}건 삽입 완료")


def main():
    print("=" * 50)
    print("실제 데이터 삽입 시작")
    print(f"데이터 경로: {DATA_DIR}")
    print("=" * 50)

    db = SessionLocal()
    try:
        # 기존 데이터 삭제
        clean_tables(db)

        # 1. ETF 데이터
        insert_etf_data(db)

        # 2. 회사 데이터
        insert_company_data(db)

        # 2.5. Industry Group 업데이트
        update_industry_groups(db)

        # 3. Stock 데이터
        insert_stock_data(db)

        # 4. ETF 구성종목
        insert_etf_compositions(db)

        print("\n" + "=" * 50)
        print("실제 데이터 삽입 완료!")
        print("=" * 50)

        # 결과 확인
        result = db.execute(text("""
            SELECT 'etf' as tbl, COUNT(*) FROM etf
            UNION ALL SELECT 'company_info', COUNT(*) FROM company_info
            UNION ALL SELECT 'stock', COUNT(*) FROM stock
            UNION ALL SELECT 'etf_stock_composition', COUNT(*) FROM etf_stock_composition
        """))
        for row in result.fetchall():
            print(f"  {row[0]}: {row[1]}건")

    finally:
        db.close()


if __name__ == "__main__":
    main()
