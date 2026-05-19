"""news_article 스키마 확인"""
import os
import psycopg

conn = psycopg.connect(os.environ["LOCAL_DATABASE_URL"])
cur = conn.cursor()
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'news_article' ORDER BY ordinal_position")
print('news_article 컬럼:')
for row in cur.fetchall():
    print(f'  - {row[0]}')
conn.close()
