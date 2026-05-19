"""
샘플 데이터 삽입 스크립트

사용법:
    cd backend/data-service
    python -m scripts.seed_sample_data
"""
import sys
from pathlib import Path

# 프로젝트 루트를 path에 추가
sys.path.insert(0, str(Path(__file__).parent.parent))

from datetime import date
from sqlalchemy import text
from app.database import SessionLocal, engine
from app.models.company import IndustryClassification, CompanyInfo
from app.models.etf import ETF, ETFComposition


def create_tables():
    """테이블 생성 (없으면)"""
    from app.database import Base
    from app.models.company import IndustryClassification, CompanyInfo
    from app.models.etf import ETF, ETFComposition, ETFSectorCluster, ETFPrice

    Base.metadata.create_all(bind=engine)
    print("테이블 생성 완료")


def seed_industry_classification(db):
    """산업분류 샘플 데이터"""

    industries = [
        # 대분류 (level 1)
        {"code": "C", "name": "제조업", "level": 1, "parent_code": None, "group_code": None, "group_name": None},
        {"code": "J", "name": "정보통신업", "level": 1, "parent_code": None, "group_code": "IT_SW", "group_name": "소프트웨어"},
        {"code": "K", "name": "금융 및 보험업", "level": 1, "parent_code": None, "group_code": "FINANCE", "group_name": "금융"},

        # 중분류 (level 2)
        {"code": "26", "name": "전자부품/컴퓨터/통신장비", "level": 2, "parent_code": "C", "group_code": "IT_ELEC", "group_name": "전자/IT"},
        {"code": "21", "name": "의료용 물질 및 의약품", "level": 2, "parent_code": "C", "group_code": "BIO", "group_name": "바이오/의약"},
        {"code": "30", "name": "자동차 및 트레일러", "level": 2, "parent_code": "C", "group_code": "AUTO", "group_name": "자동차"},
        {"code": "28", "name": "전기장비 제조업", "level": 2, "parent_code": "C", "group_code": "ENERGY", "group_name": "에너지"},

        # 소분류 (level 3)
        {"code": "261", "name": "반도체 제조업", "level": 3, "parent_code": "26", "group_code": "IT_SEMI", "group_name": "반도체"},
        {"code": "262", "name": "전자부품 제조업", "level": 3, "parent_code": "26", "group_code": "IT_ELEC", "group_name": "전자/IT"},
        {"code": "281", "name": "축전지 제조업", "level": 3, "parent_code": "28", "group_code": "ENERGY", "group_name": "에너지"},

        # 세분류 (level 4) - 커스텀
        {"code": "SEMI_MEM", "name": "메모리 반도체", "level": 4, "parent_code": "261", "group_code": "IT_SEMI", "group_name": "반도체"},
        {"code": "SEMI_SYS", "name": "시스템 반도체", "level": 4, "parent_code": "261", "group_code": "IT_SEMI", "group_name": "반도체"},
        {"code": "SEMI_EQP", "name": "반도체 장비", "level": 4, "parent_code": "261", "group_code": "IT_SEMI", "group_name": "반도체"},
        {"code": "BAT_CELL", "name": "배터리 셀", "level": 4, "parent_code": "281", "group_code": "ENERGY", "group_name": "에너지"},
        {"code": "BAT_MAT", "name": "배터리 소재", "level": 4, "parent_code": "281", "group_code": "ENERGY", "group_name": "에너지"},
    ]

    for ind in industries:
        existing = db.query(IndustryClassification).filter_by(code=ind["code"]).first()
        if not existing:
            db.add(IndustryClassification(**ind))

    db.commit()
    print(f"산업분류 {len(industries)}건 삽입 완료")


def seed_company_info(db):
    """회사정보 샘플 데이터"""

    companies = [
        # 반도체
        {"stock_code": "005930", "stock_name": "삼성전자", "market_type": "KOSPI",
         "industry_code": "SEMI_MEM", "industry_name": "메모리 반도체", "industry_group": "IT_SEMI",
         "aliases": ["삼전", "Samsung Electronics", "SEC"]},
        {"stock_code": "000660", "stock_name": "SK하이닉스", "market_type": "KOSPI",
         "industry_code": "SEMI_MEM", "industry_name": "메모리 반도체", "industry_group": "IT_SEMI",
         "aliases": ["하이닉스", "SK Hynix"]},
        {"stock_code": "042700", "stock_name": "한미반도체", "market_type": "KOSDAQ",
         "industry_code": "SEMI_EQP", "industry_name": "반도체 장비", "industry_group": "IT_SEMI",
         "aliases": ["한미반"]},
        {"stock_code": "403870", "stock_name": "HPSP", "market_type": "KOSDAQ",
         "industry_code": "SEMI_EQP", "industry_name": "반도체 장비", "industry_group": "IT_SEMI",
         "aliases": []},

        # 2차전지
        {"stock_code": "373220", "stock_name": "LG에너지솔루션", "market_type": "KOSPI",
         "industry_code": "BAT_CELL", "industry_name": "배터리 셀", "industry_group": "ENERGY",
         "aliases": ["LG엔솔", "LGES"]},
        {"stock_code": "006400", "stock_name": "삼성SDI", "market_type": "KOSPI",
         "industry_code": "BAT_CELL", "industry_name": "배터리 셀", "industry_group": "ENERGY",
         "aliases": ["SDI"]},
        {"stock_code": "247540", "stock_name": "에코프로비엠", "market_type": "KOSDAQ",
         "industry_code": "BAT_MAT", "industry_name": "배터리 소재", "industry_group": "ENERGY",
         "aliases": ["에코프로BM"]},
        {"stock_code": "003670", "stock_name": "포스코퓨처엠", "market_type": "KOSPI",
         "industry_code": "BAT_MAT", "industry_name": "배터리 소재", "industry_group": "ENERGY",
         "aliases": ["포퓨엠"]},

        # 바이오
        {"stock_code": "207940", "stock_name": "삼성바이오로직스", "market_type": "KOSPI",
         "industry_code": "21", "industry_name": "의약품 제조업", "industry_group": "BIO",
         "aliases": ["삼바"]},
        {"stock_code": "068270", "stock_name": "셀트리온", "market_type": "KOSPI",
         "industry_code": "21", "industry_name": "의약품 제조업", "industry_group": "BIO",
         "aliases": []},

        # 자동차
        {"stock_code": "005380", "stock_name": "현대자동차", "market_type": "KOSPI",
         "industry_code": "30", "industry_name": "자동차 제조업", "industry_group": "AUTO",
         "aliases": ["현대차", "Hyundai"]},
        {"stock_code": "000270", "stock_name": "기아", "market_type": "KOSPI",
         "industry_code": "30", "industry_name": "자동차 제조업", "industry_group": "AUTO",
         "aliases": ["Kia"]},
    ]

    for comp in companies:
        existing = db.query(CompanyInfo).filter_by(stock_code=comp["stock_code"]).first()
        if not existing:
            db.add(CompanyInfo(**comp))

    db.commit()
    print(f"회사정보 {len(companies)}건 삽입 완료")


def seed_etf(db):
    """ETF 샘플 데이터"""

    etfs = [
        {"stock_code": "091160", "name": "KODEX 반도체", "category": "국내주식형",
         "strategy_type": "THEME", "sector": "반도체", "asset_class": "EQUITY",
         "asset_manager": "KODEX", "is_active": True},
        {"stock_code": "091230", "name": "TIGER 반도체", "category": "국내주식형",
         "strategy_type": "THEME", "sector": "반도체", "asset_class": "EQUITY",
         "asset_manager": "TIGER", "is_active": True},
        {"stock_code": "305720", "name": "KODEX 2차전지산업", "category": "국내주식형",
         "strategy_type": "THEME", "sector": "2차전지", "asset_class": "EQUITY",
         "asset_manager": "KODEX", "is_active": True},
        {"stock_code": "364980", "name": "TIGER 2차전지테마", "category": "국내주식형",
         "strategy_type": "THEME", "sector": "2차전지", "asset_class": "EQUITY",
         "asset_manager": "TIGER", "is_active": True},
        {"stock_code": "143860", "name": "KODEX 헬스케어", "category": "국내주식형",
         "strategy_type": "THEME", "sector": "바이오", "asset_class": "EQUITY",
         "asset_manager": "KODEX", "is_active": True},
    ]

    for etf in etfs:
        existing = db.query(ETF).filter_by(stock_code=etf["stock_code"]).first()
        if not existing:
            db.add(ETF(**etf))

    db.commit()
    print(f"ETF {len(etfs)}건 삽입 완료")


def seed_etf_compositions(db):
    """ETF 구성종목 샘플 데이터"""

    # ETF ID 조회
    kodex_semi = db.query(ETF).filter_by(stock_code="091160").first()
    tiger_semi = db.query(ETF).filter_by(stock_code="091230").first()
    kodex_battery = db.query(ETF).filter_by(stock_code="305720").first()
    tiger_battery = db.query(ETF).filter_by(stock_code="364980").first()
    kodex_health = db.query(ETF).filter_by(stock_code="143860").first()

    # 회사 ID 조회
    companies = {c.stock_code: c.id for c in db.query(CompanyInfo).all()}

    today = date.today()

    compositions = [
        # KODEX 반도체
        {"etf_id": kodex_semi.id, "company_id": companies.get("005930"), "component_stock_code": "005930", "weight_pct": 30.0, "base_date": today},
        {"etf_id": kodex_semi.id, "company_id": companies.get("000660"), "component_stock_code": "000660", "weight_pct": 25.0, "base_date": today},
        {"etf_id": kodex_semi.id, "company_id": companies.get("042700"), "component_stock_code": "042700", "weight_pct": 10.0, "base_date": today},
        {"etf_id": kodex_semi.id, "company_id": companies.get("403870"), "component_stock_code": "403870", "weight_pct": 8.0, "base_date": today},

        # TIGER 반도체
        {"etf_id": tiger_semi.id, "company_id": companies.get("005930"), "component_stock_code": "005930", "weight_pct": 28.0, "base_date": today},
        {"etf_id": tiger_semi.id, "company_id": companies.get("000660"), "component_stock_code": "000660", "weight_pct": 27.0, "base_date": today},
        {"etf_id": tiger_semi.id, "company_id": companies.get("042700"), "component_stock_code": "042700", "weight_pct": 12.0, "base_date": today},

        # KODEX 2차전지
        {"etf_id": kodex_battery.id, "company_id": companies.get("373220"), "component_stock_code": "373220", "weight_pct": 22.0, "base_date": today},
        {"etf_id": kodex_battery.id, "company_id": companies.get("006400"), "component_stock_code": "006400", "weight_pct": 18.0, "base_date": today},
        {"etf_id": kodex_battery.id, "company_id": companies.get("247540"), "component_stock_code": "247540", "weight_pct": 15.0, "base_date": today},
        {"etf_id": kodex_battery.id, "company_id": companies.get("003670"), "component_stock_code": "003670", "weight_pct": 12.0, "base_date": today},

        # TIGER 2차전지
        {"etf_id": tiger_battery.id, "company_id": companies.get("373220"), "component_stock_code": "373220", "weight_pct": 25.0, "base_date": today},
        {"etf_id": tiger_battery.id, "company_id": companies.get("006400"), "component_stock_code": "006400", "weight_pct": 20.0, "base_date": today},
        {"etf_id": tiger_battery.id, "company_id": companies.get("247540"), "component_stock_code": "247540", "weight_pct": 18.0, "base_date": today},

        # KODEX 헬스케어
        {"etf_id": kodex_health.id, "company_id": companies.get("207940"), "component_stock_code": "207940", "weight_pct": 20.0, "base_date": today},
        {"etf_id": kodex_health.id, "company_id": companies.get("068270"), "component_stock_code": "068270", "weight_pct": 18.0, "base_date": today},
    ]

    # 기존 데이터 삭제 후 삽입
    db.query(ETFComposition).delete()
    for comp in compositions:
        if comp["etf_id"] and comp["company_id"]:
            db.add(ETFComposition(**comp))

    db.commit()
    print(f"ETF 구성종목 {len(compositions)}건 삽입 완료")


def main():
    print("=" * 50)
    print("샘플 데이터 삽입 시작")
    print("=" * 50)

    # 테이블 생성
    create_tables()

    db = SessionLocal()
    try:
        # 순서 중요: 참조 관계 고려
        seed_industry_classification(db)
        seed_company_info(db)
        seed_etf(db)
        seed_etf_compositions(db)

        print("=" * 50)
        print("샘플 데이터 삽입 완료!")
        print("=" * 50)

    finally:
        db.close()


if __name__ == "__main__":
    main()
