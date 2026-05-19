# 뉴스-ETF 연결 로직

> 네이버 증권 종목뉴스 기반 뉴스-ETF 연결 시스템

## 개요

기존 LLM 기반 `news_etf_influence` 분석 방식을 **폐기**하고,
**네이버 증권 종목뉴스**의 뉴스-종목 매핑을 활용하는 방식으로 변경했습니다.

### 변경 이유

| 기존 (폐기됨) | 현재 |
|---------------|------|
| LLM으로 뉴스-ETF 영향도 분석 | 네이버가 이미 뉴스-종목 매핑 제공 |
| 복잡한 프롬프트 + 비용 발생 | LLM 호출 불필요 (크롤링만) |
| `news_etf_influence` 테이블 | `news_stock_mapping` 테이블 |
| 영향도 점수 계산 | 구성종목 비중 기반 자연스러운 연결 |

---

## 데이터 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│                     네이버 증권 종목뉴스                          │
│         finance.naver.com/item/main.naver?code=005930           │
└─────────────────────┬───────────────────────────────────────────┘
                      │ 크롤링
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      news_article                               │
│  - title, content, content_summary (AI 요약)                    │
│  - keywords (AI 추출), category_code                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │ 1:N
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                   news_stock_mapping                            │
│  - news_id (FK → news_article)                                  │
│  - company_id (FK → company_info)                               │
│  ※ 하나의 뉴스가 여러 종목에 매핑 가능                            │
└─────────────────────┬───────────────────────────────────────────┘
                      │ company_id
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      company_info                               │
│  - id, company_name, industry_code, industry_group              │
└─────────────────────┬───────────────────────────────────────────┘
                      │ company_id
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                        stock                                    │
│  - id, company_id, ticker                                       │
└─────────────────────┬───────────────────────────────────────────┘
                      │ stock_id
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                  etf_stock_composition                          │
│  - etf_id, stock_id, weight_pct (구성 비중)                      │
└─────────────────────┬───────────────────────────────────────────┘
                      │ etf_id
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                          etf                                    │
│  - id, stock_code, name, sector, ...                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 핵심 쿼리

### 뉴스 → 관련 ETF 조회

```sql
SELECT DISTINCT e.* FROM etf e
JOIN etf_stock_composition esc ON e.id = esc.etf_id
JOIN stock s ON esc.stock_id = s.id
JOIN news_stock_mapping nsm ON s.company_id = nsm.company_id
WHERE nsm.news_id = :newsId
  AND e.is_active = true
ORDER BY esc.weight_pct DESC
LIMIT 5
```

**로직:**
1. `news_stock_mapping`에서 뉴스와 연결된 `company_id` 조회
2. `stock` 테이블에서 해당 회사의 주식 조회
3. `etf_stock_composition`에서 해당 주식을 포함하는 ETF 조회
4. 구성 비중(`weight_pct`) 높은 순으로 정렬

### ETF → 관련 뉴스 조회

```sql
SELECT DISTINCT na.* FROM news_article na
JOIN news_stock_mapping nsm ON na.id = nsm.news_id
JOIN stock s ON nsm.company_id = s.company_id
JOIN etf_stock_composition esc ON s.id = esc.stock_id
WHERE esc.etf_id = :etfId
  AND na.is_active = true
ORDER BY na.published_at DESC
LIMIT 20
```

---

## 크롤링 프로세스

### 1. 종목별 뉴스 크롤링

```python
# stock_news_scraper.py

async def scrape_stock_news(stock_code: str):
    # 1. 종목 메인 페이지에서 뉴스 링크 추출
    #    finance.naver.com/item/main.naver?code=005930

    # 2. news_read.naver → redirect URL 추출
    #    실제 뉴스 URL (n.news.naver.com) 획득

    # 3. n.news.naver.com에서 본문 추출
    #    - 제목, 본문, 언론사, 썸네일, 발행일

    # 4. DB 저장
    #    - news_article 저장
    #    - news_stock_mapping 저장 (뉴스-종목 매핑)
```

### 2. 카테고리 자동 분류

크롤링 시 종목의 `industry_group`을 기반으로 뉴스 카테고리 자동 설정:

```python
industry_group → category_code 매핑:
- IT_SEMI  → NEWS_SEMI (반도체)
- IT_SW    → NEWS_IT (IT/전자)
- BIO      → NEWS_BIO (바이오/의약)
- AUTO     → NEWS_AUTO (자동차)
- CHEM     → NEWS_CHEM (화학/소재)
- ENERGY   → NEWS_ENERGY (에너지)
- FINANCE  → NEWS_FINANCE (금융)
- CONSTRUCT→ NEWS_CONSTRUCT (건설/부동산)
- CONSUMER → NEWS_CONSUMER (소비재)
- TELECOM  → NEWS_TELECOM (통신/미디어)
- TRANSPORT→ NEWS_TRANSPORT (운송/물류)
- 기타     → NEWS_ETC
```

---

## AI 분석 (선택적)

뉴스-종목 매핑은 네이버가 제공하므로 **LLM 분석은 요약/키워드 추출에만 사용**:

| 필드 | 설명 | 예시 |
|------|------|------|
| `content_summary` | AI 핵심 요약 (3줄) | `{"bullets": ["삼성전자 HBM3E 양산", "SK하이닉스와 경쟁", "2분기 실적 개선"]}` |
| `keywords` | AI 키워드 추출 | `["HBM", "삼성전자", "메모리반도체", "AI가속기"]` |

---

## 테이블 구조

### news_stock_mapping

```sql
CREATE TABLE "news_stock_mapping" (
    "id" BIGSERIAL PRIMARY KEY,
    "news_id" BIGINT NOT NULL,      -- news_article FK
    "company_id" BIGINT NOT NULL,   -- company_info FK
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "uk_news_stock" UNIQUE ("news_id", "company_id")
);
```

### 관계도

```
news_article (1) ──┬── (N) news_stock_mapping (N) ──┬── (1) company_info
                   │                                │
                   │                                ▼
                   │                         stock (1) ──── (N) etf_stock_composition
                   │                                              │
                   │                                              ▼
                   └──────────────────────────────────────── etf (관련 ETF)
```

---

## 폐기된 시스템

### news_etf_influence (삭제됨)

기존에 계획했던 LLM 기반 영향도 분석:

```sql
-- 삭제됨
CREATE TABLE "news_etf_influence" (
    "news_id" BIGINT,
    "etf_id" BIGINT,
    "influence_score" DECIMAL,  -- LLM이 계산한 영향도
    "influence_reason" TEXT,    -- 영향 이유
    ...
);
```

**폐기 사유:**
1. 네이버가 이미 뉴스-종목 매핑 제공
2. LLM 호출 비용 절감
3. 구성종목 비중 기반 연결이 더 신뢰성 있음

---

## 참고

- **크롤러**: `backend/data-service/app/scrapers/stock_news_scraper.py`
- **모델**: `backend/data-service/app/models/news_stock.py`
- **API 서비스**: `backend/user-service/.../NewsServiceImpl.java`
- **Repository 쿼리**: `backend/user-service/.../EtfRepository.java`
