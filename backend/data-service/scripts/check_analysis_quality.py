"""AI 분석 결과 품질 확인"""
import os
import psycopg
import json

conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()

# 현황
cur.execute("SELECT COUNT(*) FROM news_article")
total = cur.fetchone()[0]

cur.execute("SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL")
analyzed = cur.fetchone()[0]

print(f"=== AI 분석 현황 ===")
print(f"총 뉴스: {total}개")
print(f"분석 완료: {analyzed}개 ({analyzed/total*100:.1f}%)")
print(f"미분석: {total - analyzed}개")

# 샘플 3개 확인
print(f"\n=== 분석 결과 샘플 ===\n")
cur.execute("""
    SELECT id, title, content_summary, keywords
    FROM news_article
    WHERE content_summary IS NOT NULL
    ORDER BY created_at DESC
    LIMIT 3
""")

for i, (id, title, summary, keywords) in enumerate(cur.fetchall(), 1):
    print(f"[{i}] ID: {id}")
    print(f"제목: {title[:60]}...")

    if summary:
        if isinstance(summary, str):
            summary = json.loads(summary)
        bullets = summary.get('bullets', [])
        print("요약:")
        for b in bullets:
            print(f"  • {b}")

    if keywords:
        if isinstance(keywords, str):
            keywords = json.loads(keywords)
        print(f"키워드: {', '.join(keywords)}")

    print()

# ETF 추천 확인
print("=== ETF 추천 샘플 ===\n")
cur.execute("""
    SELECT na.title, e.etf_name, nei.influence_score, nei.influence_type
    FROM news_etf_influence nei
    JOIN news_article na ON nei.news_id = na.id
    JOIN etf e ON nei.etf_id = e.id
    ORDER BY nei.created_at DESC
    LIMIT 10
""")

for title, etf_name, score, inf_type in cur.fetchall():
    print(f"  [{inf_type}] {etf_name}: {score:.2f} - {title[:40]}...")

conn.close()
