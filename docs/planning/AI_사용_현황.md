# AI 사용 현황

## 개요
프로젝트에서 사용 중인 AI 기능 및 API 연동 현황

## API 키 설정

### 환경변수 (.env)
```bash
# Anthropic API (직접 호출 - 우선 사용)
ANTHROPIC_API_KEY=sk-ant-xxx

# OpenAI API (직접 호출)
OPENAI_API_KEY=sk-proj-xxx

# SSAFY GMS (대체용 - API 키 없을 때)
GMS_API_KEY=xxx
GMS_API_URL=https://your-gms-endpoint.example.com
```

### 우선순위
1. `ANTHROPIC_API_KEY` 있으면 → Anthropic 직접 호출
2. 없으면 → GMS 경유 (SSAFY 크레딧 사용)

---

## AI 기능 목록

### 1. 포트폴리오 AI 피드백
| 항목 | 내용 |
|------|------|
| **기능** | 사용자 포트폴리오 분석 및 투자 성향 진단 |
| **서비스** | user-service (Java) |
| **파일** | `LlmServiceImpl.java` |
| **프롬프트** | `portfolio_feedback` (DB: ai_prompt 테이블) |
| **모델** | claude-3-haiku-20240307 |
| **호출 시점** | 포트폴리오 리뷰 요청 시 |

**응답 형식:**
```json
{
  "headline": "공격적인 수익 추구!",
  "sub_headline": "기술주 중심의 로켓 포트폴리오",
  "keywords": ["기술주집중", "고변동성", "성장중심"],
  "analysis": "이 포트폴리오는..."
}
```

---

### 2. 섹터 버블 AI 분석
| 항목 | 내용 |
|------|------|
| **기능** | ETF 클러스터 뷰에서 섹터 버블 클릭 시 AI 분석 표시 |
| **서비스** | data-service (Python) |
| **파일** | `scripts/generate_sector_bubble_ai.py` |
| **프롬프트** | `sector_bubble_analysis` (DB: ai_prompt 테이블) |
| **모델** | claude-3-haiku-20240307 (기본) |
| **호출 시점** | 배치 스크립트 실행 시 (사전 생성) |
| **저장 테이블** | `etf_sector_ai_history` |

**응답 형식:**
```json
{
  "analysis": "반도체 섹터의 높은 기여도로 인해...",
  "risk_level": "HIGH",
  "key_point": "집중투자"
}
```

**실행 방법:**
```bash
cd backend/data-service
python -m scripts.generate_sector_bubble_ai
python -m scripts.generate_sector_bubble_ai --etf-id 1  # 특정 ETF만
python -m scripts.generate_sector_bubble_ai --force     # 재생성
```

---

### 3. 뉴스-ETF 연결 (LLM 미사용)

> ⚠️ **참고**: 뉴스 분석은 LLM을 사용하지 않습니다.

| 항목 | 내용 |
|------|------|
| **방식** | 네이버 증권 종목뉴스 크롤링 (LLM 분석 불필요) |
| **매핑** | 네이버가 이미 뉴스-종목 매핑 제공 |
| **저장 테이블** | `news_article`, `news_stock_mapping` |
| **ETF 연결** | `news_stock_mapping` → `etf_stock_composition` 조인 |

**데이터 흐름:**
```
네이버 증권 종목뉴스 크롤링
       ↓
news_article (뉴스 기사)
news_stock_mapping (뉴스-종목 매핑)
       ↓
ETF 관련 뉴스 = news_stock_mapping
              JOIN etf_stock_composition
              (ETF 구성종목의 뉴스)
```

**참고 문서:** `docs/planning/뉴스_ETF_영향력_설계.md`

---

## 비용 비교

### Anthropic (직접 호출)
| 모델 | Input | Output | 1건당 예상 |
|------|-------|--------|-----------|
| claude-3-haiku | $0.25/1M | $1.25/1M | **$0.00036** |
| claude-3.5-sonnet | $3/1M | $15/1M | $0.0045 |

### OpenAI (직접 호출)
| 모델 | Input | Output | 1건당 예상 |
|------|-------|--------|-----------|
| gpt-4o-mini | $0.15/1M | $0.60/1M | **$0.00020** |
| gpt-4o | $2.50/1M | $10/1M | $0.0032 |

### SSAFY GMS (크레딧)
| 모델 | 크레딧/건 |
|------|----------|
| claude-3-haiku | 10 Credit |
| gpt-4o-mini | 2 Credit |
| claude-sonnet-4 | 30 Credit |

---

## 파일 구조

### data-service (Python)
```
backend/data-service/
├── app/
│   ├── config.py                    # API 키 설정
│   └── services/
│       └── llm_service.py           # LLM 호출 공통 서비스
└── scripts/
    └── generate_sector_bubble_ai.py # 섹터 버블 AI 배치
```

### user-service (Java)
```
backend/user-service/src/main/java/.../
├── common/config/
│   └── GmsConfig.java               # WebClient 설정
└── domain/ai/
    ├── dto/
    │   ├── GmsRequest.java          # API 요청 DTO
    │   └── GmsResponse.java         # API 응답 DTO
    ├── entity/
    │   └── PortfolioAiFeedback.java # 피드백 엔티티
    └── service/impl/
        └── LlmServiceImpl.java      # LLM 서비스 구현
```

---

## 프롬프트 관리

프롬프트는 DB `ai_prompt` 테이블에서 관리:
```sql
SELECT name, version, is_active FROM ai_prompt;
```

| name | version | 용도 |
|------|---------|------|
| portfolio_feedback | v1.0 | 포트폴리오 AI 피드백 |
| sector_bubble_analysis | v1.0 | 섹터 버블 AI 분석 |

프롬프트 추가/수정: `db/prompts.sql`

---

## 생성 현황

| 기능 | 대상 | 완료 건수 | 비고 |
|------|------|----------|------|
| 섹터 버블 AI 분석 | ETF 1,602개 | 1,535건 | 완료 |
| 포트폴리오 AI 피드백 | 사용자 요청 시 | 실시간 | - |
