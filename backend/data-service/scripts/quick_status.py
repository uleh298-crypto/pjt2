import os
import psycopg
conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()
cur.execute("SELECT COUNT(*) FROM news_article")
total = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL")
analyzed = cur.fetchone()[0]
print(f"총: {total}, 완료: {analyzed} ({analyzed/total*100:.1f}%), 미완료: {total-analyzed}")
conn.close()
