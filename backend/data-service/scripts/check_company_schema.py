"""company_info 스키마 확인"""
import os
import psycopg

conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()

# 컬럼 목록
cur.execute("""
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns
    WHERE table_name = 'company_info'
    ORDER BY ordinal_position
""")
print("=== company_info 컬럼 ===")
for row in cur.fetchall():
    print(f"  {row[0]}: {row[1]} (nullable: {row[2]})")

# 샘플 데이터
print("\n=== 샘플 데이터 (상위 5개) ===")
cur.execute("SELECT * FROM company_info LIMIT 5")
cols = [desc[0] for desc in cur.description]
print(f"컬럼: {cols}")
for row in cur.fetchall():
    print(f"  {row}")

conn.close()
