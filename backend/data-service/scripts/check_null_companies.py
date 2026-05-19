"""company_name이 NULL인 회사 확인"""
import os
import psycopg

conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()

# company_name이 NULL인 회사
cur.execute("""
    SELECT id, stock_code, company_name, industry_code, industry_group
    FROM company_info
    WHERE company_name IS NULL
    LIMIT 20
""")
null_names = cur.fetchall()

print(f"=== company_name이 NULL인 회사 (처음 20개) ===")
for row in null_names:
    print(f"  ID: {row[0]}, 종목코드: {row[1]}, 이름: {row[2]}, 산업코드: {row[3]}, 그룹: {row[4]}")

# 전체 통계
cur.execute("SELECT COUNT(*) FROM company_info WHERE company_name IS NULL")
null_count = cur.fetchone()[0]

cur.execute("SELECT COUNT(*) FROM company_info WHERE company_name IS NOT NULL")
valid_count = cur.fetchone()[0]

cur.execute("SELECT COUNT(*) FROM company_info WHERE industry_code IS NULL")
no_industry = cur.fetchone()[0]

cur.execute("SELECT COUNT(*) FROM company_info WHERE industry_code IS NOT NULL")
has_industry = cur.fetchone()[0]

print(f"\n=== 통계 ===")
print(f"전체 회사: {null_count + valid_count}개")
print(f"  - company_name 있음: {valid_count}개")
print(f"  - company_name NULL: {null_count}개")
print(f"  - industry_code 있음: {has_industry}개")
print(f"  - industry_code NULL: {no_industry}개")

conn.close()
