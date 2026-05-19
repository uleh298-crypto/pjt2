"""
매핑 테스트 스크립트

회사 ↔ 산업분류, ETF ↔ 회사 구성종목 매핑 테스트

사용법:
    cd backend/data-service
    python -m scripts.test_mapping
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import text
from app.database import SessionLocal
from app.models.company import IndustryClassification, CompanyInfo
from app.models.etf import ETF, ETFComposition, ETFSectorCluster


def test_company_industry_mapping(db):
    """회사 ↔ 산업분류 매핑 테스트"""
    print("\n" + "=" * 60)
    print("1. 회사 ↔ 산업분류 매핑 테스트")
    print("=" * 60)

    # 모든 회사와 산업분류 조인
    query = text("""
        SELECT
            c.stock_code,
            c.stock_name,
            c.industry_code,
            c.industry_name,
            c.industry_group,
            ic.name as industry_full_name,
            ic.group_code,
            ic.group_name,
            ic.level
        FROM company_info c
        LEFT JOIN industry_classification ic ON c.industry_code = ic.code
        ORDER BY c.industry_group, c.stock_code
    """)

    results = db.execute(query).fetchall()

    current_group = None
    for row in results:
        if row.industry_group != current_group:
            current_group = row.industry_group
            print(f"\n[{row.group_name or row.industry_group}]")

        status = "✓" if row.group_code else "✗ (매핑 안됨)"
        print(f"  {row.stock_code} {row.stock_name:12} → {row.industry_code or 'NULL':10} (level {row.level or '-'}) {status}")

    # 매핑 통계
    total = len(results)
    mapped = len([r for r in results if r.group_code])
    print(f"\n매핑 완료: {mapped}/{total} ({mapped/total*100:.1f}%)")


def test_etf_composition_mapping(db):
    """ETF ↔ 회사 구성종목 매핑 테스트"""
    print("\n" + "=" * 60)
    print("2. ETF ↔ 회사 구성종목 매핑 테스트")
    print("=" * 60)

    # ETF별 구성종목 조회
    query = text("""
        SELECT
            e.stock_code as etf_code,
            e.name as etf_name,
            e.sector,
            ec.component_stock_code,
            ec.weight_pct,
            c.stock_name,
            c.industry_group
        FROM etf e
        JOIN etf_compositions ec ON e.id = ec.etf_id
        LEFT JOIN company_info c ON ec.company_id = c.id
        ORDER BY e.stock_code, ec.weight_pct DESC
    """)

    results = db.execute(query).fetchall()

    current_etf = None
    for row in results:
        if row.etf_code != current_etf:
            current_etf = row.etf_code
            print(f"\n[{row.etf_code}] {row.etf_name} ({row.sector})")

        status = "✓" if row.stock_name else "✗ (회사 매핑 안됨)"
        print(f"  {row.component_stock_code} {row.stock_name or 'N/A':12} {row.weight_pct:5.1f}% [{row.industry_group or '-':10}] {status}")

    # 매핑 통계
    total = len(results)
    mapped = len([r for r in results if r.stock_name])
    print(f"\n매핑 완료: {mapped}/{total} ({mapped/total*100:.1f}%)")


def test_etf_sector_aggregation(db):
    """ETF 섹터별 비중 집계 테스트"""
    print("\n" + "=" * 60)
    print("3. ETF 섹터별 비중 집계 테스트")
    print("=" * 60)

    # ETF별 group_code 비중 집계
    query = text("""
        SELECT
            e.stock_code as etf_code,
            e.name as etf_name,
            c.industry_group,
            ic.group_name,
            SUM(ec.weight_pct) as total_weight,
            COUNT(*) as stock_count
        FROM etf e
        JOIN etf_compositions ec ON e.id = ec.etf_id
        JOIN company_info c ON ec.company_id = c.id
        JOIN industry_classification ic ON c.industry_code = ic.code
        GROUP BY e.stock_code, e.name, c.industry_group, ic.group_name
        ORDER BY e.stock_code, total_weight DESC
    """)

    results = db.execute(query).fetchall()

    current_etf = None
    for row in results:
        if row.etf_code != current_etf:
            current_etf = row.etf_code
            print(f"\n[{row.etf_code}] {row.etf_name}")

        print(f"  {row.group_name or row.industry_group:12} {row.total_weight:5.1f}% ({row.stock_count}종목)")


def test_news_etf_scenario(db):
    """뉴스 → ETF 매핑 시나리오 테스트"""
    print("\n" + "=" * 60)
    print("4. 뉴스 → ETF 매핑 시나리오 테스트")
    print("=" * 60)

    # 시나리오: "삼성전자 관련 뉴스"가 영향을 주는 ETF 찾기
    test_company = "삼성전자"
    print(f"\n시나리오: '{test_company}' 관련 뉴스 → 어떤 ETF에 영향?")

    query = text("""
        SELECT DISTINCT
            e.stock_code,
            e.name as etf_name,
            e.sector,
            ec.weight_pct as company_weight
        FROM company_info c
        JOIN etf_compositions ec ON c.id = ec.company_id
        JOIN etf e ON ec.etf_id = e.id
        WHERE c.stock_name = :company_name
        ORDER BY ec.weight_pct DESC
    """)

    results = db.execute(query, {"company_name": test_company}).fetchall()

    for row in results:
        print(f"  → [{row.stock_code}] {row.etf_name} (비중: {row.company_weight}%)")

    # 시나리오: "반도체 산업 뉴스"가 영향을 주는 ETF 찾기
    test_group = "IT_SEMI"
    print(f"\n시나리오: '{test_group}' 산업 뉴스 → 어떤 ETF에 영향?")

    query = text("""
        SELECT DISTINCT
            e.stock_code,
            e.name as etf_name,
            e.sector,
            SUM(ec.weight_pct) as sector_weight
        FROM etf e
        JOIN etf_compositions ec ON e.id = ec.etf_id
        JOIN company_info c ON ec.company_id = c.id
        WHERE c.industry_group = :group_code
        GROUP BY e.stock_code, e.name, e.sector
        HAVING SUM(ec.weight_pct) >= 10
        ORDER BY sector_weight DESC
    """)

    results = db.execute(query, {"group_code": test_group}).fetchall()

    for row in results:
        print(f"  → [{row.stock_code}] {row.etf_name} (섹터 비중: {row.sector_weight}%)")


def main():
    print("=" * 60)
    print("매핑 테스트 시작")
    print("=" * 60)

    db = SessionLocal()
    try:
        test_company_industry_mapping(db)
        test_etf_composition_mapping(db)
        test_etf_sector_aggregation(db)
        test_news_etf_scenario(db)

        print("\n" + "=" * 60)
        print("매핑 테스트 완료!")
        print("=" * 60)

    finally:
        db.close()


if __name__ == "__main__":
    main()
