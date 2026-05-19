"""최종 현황 확인"""
import os
import psycopg

# 로컬 DB
local = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = local.cursor()

print("=== 최종 데이터 현황 (로컬 DB) ===\n")

# 회사 정보
cur.execute("SELECT COUNT(*) FROM company_info")
total_companies = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM company_info WHERE industry_code IS NOT NULL")
with_code = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM company_info WHERE industry_group IS NOT NULL")
with_group = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM company_info WHERE stock_name IS NOT NULL")
with_name = cur.fetchone()[0]

print(f"회사 (company_info): {total_companies}개")
print(f"  - stock_name 있음: {with_name}개 ({with_name/total_companies*100:.1f}%)")
print(f"  - industry_code 있음: {with_code}개 ({with_code/total_companies*100:.1f}%)")
print(f"  - industry_group 있음: {with_group}개 ({with_group/total_companies*100:.1f}%)")

# industry_code 미설정 회사 샘플
print(f"\n=== industry_code 미설정 회사 샘플 (총 {total_companies - with_code}개) ===")
cur.execute("""
    SELECT stock_code, stock_name, industry_group
    FROM company_info
    WHERE industry_code IS NULL AND stock_name IS NOT NULL
    LIMIT 20
""")
for row in cur.fetchall():
    print(f"  {row[0]}: {row[1]} (group: {row[2]})")

# ETF
cur.execute("SELECT COUNT(*) FROM etf")
print(f"\nETF: {cur.fetchone()[0]}개")

# 뉴스
cur.execute("SELECT COUNT(*) FROM news_article")
total_news = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL")
analyzed_news = cur.fetchone()[0]
print(f"뉴스: {total_news}개")
print(f"  - AI 분석 완료: {analyzed_news}개 ({analyzed_news/total_news*100:.1f}%)")

# 매핑
cur.execute("SELECT COUNT(*) FROM news_stock_mapping")
print(f"뉴스-종목 매핑: {cur.fetchone()[0]}건")

cur.execute("SELECT COUNT(*) FROM news_etf_influence")
print(f"뉴스-ETF 영향도: {cur.fetchone()[0]}건")

cur.execute("SELECT COUNT(*) FROM etf_sector_cluster")
print(f"ETF 섹터 클러스터: {cur.fetchone()[0]}건")

local.close()
