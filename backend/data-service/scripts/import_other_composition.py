"""
etf_other_composition 테이블에 선물/파생상품 데이터 import

대상: 주식이 아닌 자산을 보유한 ETF (선물, ETF, 우선주 등)
"""

import os
import sys
import csv
import re

# 프로젝트 루트를 path에 추가
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from sqlalchemy.orm import Session
from app.database import SessionLocal
from app.models.etf import Etf


def classify_asset(ticker: str) -> tuple[str, str]:
    """
    티커 코드로 자산 유형 분류
    Returns: (asset_type, identifier_type)
    """
    # 선물 코드 패턴
    if ticker.startswith('A') and len(ticker) == 6:
        # A01630: KOSPI200 선물, A06630: 코스닥150 선물, A75630: USD 선물
        return 'FUTURES', 'KRX_CODE'

    if re.match(r'^[A-Z]{2,3}[A-Z]\d$', ticker):
        # GCJ6: 금 선물, BQJ6: 은행주 선물 등 (해외선물)
        return 'FUTURES', 'CME_CODE'

    if re.match(r'^\d{4}[A-Z]\d$', ticker):
        # 8463V1, 4347Y6: 채권/RP
        return 'BOND', 'INTERNAL_CODE'

    if ticker == 'ZZ0000' or ticker == '010010':
        # 현금성 자산
        return 'CASH', 'INTERNAL_CODE'

    # 6자리 숫자 - 주식 또는 ETF
    if re.match(r'^\d{6}$', ticker):
        # 끝자리가 5면 우선주
        if ticker.endswith('5') or ticker[-2] == '8' or 'K' in ticker:
            return 'PREFERRED_STOCK', 'STOCK_CODE'
        # ETF 코드 범위 확인 (대략적으로)
        code_num = int(ticker)
        if 100000 <= code_num < 500000:
            return 'ETF', 'STOCK_CODE'
        return 'STOCK', 'STOCK_CODE'

    # 우선주 코드 (00680K 등)
    if re.match(r'^\d{5}K$', ticker):
        return 'PREFERRED_STOCK', 'STOCK_CODE'

    return 'OTHER', 'UNKNOWN'


def get_asset_name(ticker: str, asset_type: str) -> str:
    """자산명 반환"""
    futures_names = {
        'A01630': 'KOSPI200 선물',
        'A06630': '코스닥150 선물',
        'A75630': 'USD 선물',
    }

    if ticker in futures_names:
        return futures_names[ticker]

    if ticker.startswith('GC'):
        return '금 선물'
    if ticker.startswith('BQ'):
        return '은행주 선물'

    if ticker == 'ZZ0000' or ticker == '010010':
        return '현금성 자산'

    if asset_type == 'BOND':
        return '채권/RP'

    return ticker  # 기본값: 티커 그대로


def import_other_composition(db: Session, csv_dir: str):
    """CSV 파일에서 etf_other_composition 데이터 import"""

    # composition이 없는 ETF 목록
    target_etfs = [
        '114800', '123310', '132030', '250780', '251340', '252670',
        '252710', '261140', '267770', '280940', '319640', '360150'
    ]

    total_inserted = 0

    for stock_code in target_etfs:
        csv_path = os.path.join(csv_dir, f"{stock_code}.csv")

        if not os.path.exists(csv_path):
            print(f"[SKIP] {stock_code}.csv 파일 없음")
            continue

        # ETF ID 조회
        etf = db.query(Etf).filter(Etf.stock_code == stock_code).first()
        if not etf:
            print(f"[SKIP] ETF {stock_code} DB에 없음")
            continue

        # CSV 읽기
        with open(csv_path, 'r', encoding='utf-8-sig') as f:
            reader = csv.DictReader(f)
            rows_inserted = 0

            for row in reader:
                ticker = row['티커'].strip()
                quantity = float(row['계약수'])
                market_value = int(float(row['금액']))
                weight = float(row['비중'])

                # 자산 분류
                asset_type, identifier_type = classify_asset(ticker)
                asset_name = get_asset_name(ticker, asset_type)

                # 주식은 etf_stock_composition에 들어가야 하므로 스킵
                # 단, 우선주나 ETF 내 ETF는 여기서 처리
                if asset_type == 'STOCK':
                    continue

                # INSERT
                db.execute(
                    """
                    INSERT INTO etf_other_composition
                    (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
                    VALUES (:etf_id, :asset_type, :asset_name, :identifier_type, :identifier_value, :weight, :market_value)
                    """,
                    {
                        'etf_id': etf.id,
                        'asset_type': asset_type,
                        'asset_name': asset_name,
                        'identifier_type': identifier_type,
                        'identifier_value': ticker,
                        'weight': weight,
                        'market_value': market_value
                    }
                )
                rows_inserted += 1

            print(f"[OK] {stock_code} ({etf.name}): {rows_inserted}건 추가")
            total_inserted += rows_inserted

    db.commit()
    print(f"\n총 {total_inserted}건 import 완료")
    return total_inserted


def main():
    csv_dir = os.path.join(
        os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))),
        'docs', 'sql', 'data', 'pdf_datas'
    )

    print(f"CSV 디렉토리: {csv_dir}")
    print("=" * 50)

    db = SessionLocal()
    try:
        # 기존 데이터 삭제 (재실행 대비)
        db.execute("DELETE FROM etf_other_composition")
        db.commit()
        print("기존 etf_other_composition 데이터 삭제\n")

        import_other_composition(db, csv_dir)
    finally:
        db.close()


if __name__ == "__main__":
    main()
