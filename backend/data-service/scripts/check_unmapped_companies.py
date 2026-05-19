"""industry_code 미매핑 회사 분석"""
import os
import psycopg

conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()

# 미매핑 회사 조회 (이름이 있는 것만)
cur.execute("""
    SELECT stock_code, company_name
    FROM company_info
    WHERE industry_code IS NULL
      AND company_name IS NOT NULL
    ORDER BY company_name
""")
unmapped = cur.fetchall()

print(f"=== industry_code 미매핑 회사: {len(unmapped)}개 ===\n")

# 샘플 출력
print("처음 30개 샘플:")
for code, name in unmapped[:30]:
    print(f"  {code}: {name}")

# SPAC 체크
spac_count = sum(1 for _, name in unmapped if name and ('SPAC' in name.upper() or '스팩' in name))
print(f"\nSPAC 종목: {spac_count}개")

# 기타 패턴 분석
patterns = {
    '인버스': 0,
    '레버리지': 0,
    '리츠': 0,
    '금융': 0,
    '보험': 0,
    '증권': 0,
    '은행': 0,
    '홀딩스': 0,
    '지주': 0,
}

for _, name in unmapped:
    if name:
        for pattern in patterns:
            if pattern in name:
                patterns[pattern] += 1

print("\n패턴별 분포:")
for pattern, count in patterns.items():
    if count > 0:
        print(f"  {pattern}: {count}개")

conn.close()
