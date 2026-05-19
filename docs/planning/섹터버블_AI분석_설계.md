# 섹터 버블 AI 분석 설계

> ETF 클러스터 뷰에서 섹터 버블 클릭 시 표시되는 AI 분석 결과 생성 로직

---

## 1. 개요

### 1.1 화면 흐름

```
[ETF 상세] → [클러스터 탭] → [섹터 버블 클릭] → [바텀시트]
                                                    │
                                                    ├── 섹터명: "반도체 산업"
                                                    ├── 주요 구성 종목 목록
                                                    └── AI 분석 결과 ◄── 이 부분
```

### 1.2 AI 분석 예시

```
┌─────────────────────────────────────────────────────────────┐
│ 💡 AI 분석 결과                                              │
├─────────────────────────────────────────────────────────────┤
│ 반도체 섹터의 높은 기여도로 인해 IT 업황 회복 시 강한 반등이  │
│ 예상됩니다. 단, 상위 2개 종목 비중이 80% 이상으로 집중도가    │
│ 높은 점을 유의하세요.                                        │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 핵심 특징

- **(ETF, 섹터)** 조합별 고유한 분석
- 같은 "반도체" 섹터라도 ETF마다 다른 분석 결과
- 구성종목, 비중, 집중도 등 실제 데이터 기반

---

## 2. 데이터 구조

### 2.1 테이블 분리 설계

버블 시각화용 데이터와 AI 분석 이력을 분리하여 관리합니다.

```
┌─────────────────────────────────┐     ┌─────────────────────────────────┐
│ etf_sector_cluster              │     │ etf_sector_ai_history           │
│ (버블 시각화용, 현재 스냅샷)      │     │ (AI 분석 이력)                   │
├─────────────────────────────────┤     ├─────────────────────────────────┤
│ - etf_id                        │     │ - etf_id                        │
│ - group_code, group_name        │────►│ - group_code, group_name        │
│ - weight_pct, stock_count       │     │ - weight_pct (스냅샷)           │
│ - pos_x, pos_y, radius          │     │ - top_stocks (JSONB)            │
│ - base_date                     │     │ - ai_analysis                   │
│                                 │     │ - risk_level, key_point         │
│ ※ AI 컬럼 없음 (재계산 가능)    │     │ - base_date, created_at         │
└─────────────────────────────────┘     └─────────────────────────────────┘
```

### 2.2 etf_sector_cluster (버블 시각화용)

```sql
CREATE TABLE "etf_sector_cluster" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "cluster_type" VARCHAR(20) NOT NULL,   -- GROUP_CODE / ASSET_TYPE
    "group_code" VARCHAR(20),              -- IT_SEMI, FINANCE 등
    "group_name" VARCHAR(50),              -- "반도체", "금융"
    "weight_pct" DECIMAL(6,3) NOT NULL,    -- 28.4
    "stock_count" INTEGER,                 -- 12
    "pos_x" DECIMAL(10,6),                 -- 버블 X좌표
    "pos_y" DECIMAL(10,6),                 -- 버블 Y좌표
    "radius" DECIMAL(10,6),                -- 버블 반지름
    "base_date" DATE NOT NULL,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_cluster_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id")
);
-- ※ AI 컬럼 없음: 비중/좌표는 etf_stock_composition에서 재계산 가능
```

### 2.3 etf_sector_ai_history (AI 분석 이력)

```sql
CREATE TABLE "etf_sector_ai_history" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,
    "group_code" VARCHAR(20) NOT NULL,     -- IT_SEMI, FINANCE 등
    "group_name" VARCHAR(50),
    -- 분석 시점 스냅샷 (리밸런싱 전후 비교용)
    "weight_pct" DECIMAL(6,3),             -- 분석 시점 비중
    "stock_count" INTEGER,                 -- 분석 시점 종목 수
    "top_stocks" JSONB,                    -- [{"name":"삼성전자","weight":25.0}, ...]
    -- AI 분석 결과
    "ai_analysis" TEXT NOT NULL,           -- 분석 결과 텍스트
    "risk_level" VARCHAR(10),              -- LOW / MEDIUM / HIGH
    "key_point" VARCHAR(50),               -- "집중투자", "분산투자" 등
    "prompt_id" BIGINT,                    -- 사용된 프롬프트 FK
    -- 시점
    "base_date" DATE NOT NULL,             -- ETF 데이터 기준일
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "fk_sector_ai_etf" FOREIGN KEY ("etf_id") REFERENCES "etf"("id"),
    CONSTRAINT "fk_sector_ai_prompt" FOREIGN KEY ("prompt_id") REFERENCES "ai_prompt"("id")
);

CREATE INDEX "idx_sector_ai_lookup" ON "etf_sector_ai_history"("etf_id", "group_code", "base_date" DESC);
```

### 2.4 분리 이유

| 항목 | etf_sector_cluster | etf_sector_ai_history |
|------|-------------------|----------------------|
| **용도** | 버블 그리기 | AI 분석 표시 |
| **이력** | 불필요 (재계산 가능) | 필요 (LLM 비용) |
| **갱신** | 리밸런싱 시 덮어쓰기 | 새 레코드 INSERT |
| **데이터** | 좌표, 비중 | 분석 텍스트, 스냅샷 |

---

## 3. AI 분석 입력 데이터

### 3.1 분석에 필요한 정보

| 항목 | 예시 | 용도 |
|------|------|------|
| ETF 기본 정보 | KODEX 200, 시장형 | 맥락 제공 |
| 섹터 정보 | 반도체 (IT_SEMI), 28.4% | 분석 대상 |
| 상위 종목 | 삼성전자 25%, SK하이닉스 15.2% | 집중도 분석 |
| 종목 수 | 12개 | 분산도 분석 |
| 섹터 내 비중 분포 | 상위 2개 = 80% | 리스크 분석 |
| 최근 섹터 동향 | (선택) 뉴스 기반 | 시장 맥락 |

### 3.2 데이터 수집 쿼리

```sql
-- 섹터 버블 AI 분석용 데이터 수집
WITH sector_stocks AS (
    SELECT
        ci.company_name,
        ci.stock_code,
        esc.weight_pct,
        ROW_NUMBER() OVER (ORDER BY esc.weight_pct DESC) as rank
    FROM etf_stock_composition esc
    JOIN stock s ON s.id = esc.stock_id
    JOIN company_info ci ON ci.id = s.company_id
    JOIN industry_classification ic ON ic.code = ci.industry_code
    WHERE esc.etf_id = :etf_id
      AND ic.group_code = :group_code
      AND esc.base_date = :base_date
)
SELECT
    -- ETF 정보
    e.name as etf_name,
    e.category as etf_category,
    e.sector as etf_sector,

    -- 섹터 클러스터 정보
    esc.group_code,
    esc.group_name,
    esc.weight_pct as sector_weight,
    esc.stock_count,

    -- 상위 종목 (JSON 배열)
    (SELECT json_agg(json_build_object(
        'name', company_name,
        'weight', weight_pct
    ) ORDER BY rank)
    FROM sector_stocks WHERE rank <= 5) as top_stocks,

    -- 집중도 (상위 2개 비중 합)
    (SELECT SUM(weight_pct) FROM sector_stocks WHERE rank <= 2) as top2_concentration,

    -- HHI (허핀달-허쉬만 지수)
    (SELECT SUM(POWER(weight_pct/100, 2)) FROM sector_stocks) as hhi

FROM etf_sector_cluster esc
JOIN etf e ON e.id = esc.etf_id
WHERE esc.etf_id = :etf_id
  AND esc.group_code = :group_code
  AND esc.base_date = :base_date;
```

---

## 4. 프롬프트 설계

### 4.1 프롬프트 템플릿

```sql
-- ai_prompt 테이블에 INSERT
INSERT INTO ai_prompt (name, version, prompt_template, description, is_active)
VALUES (
    'sector_bubble_analysis',
    'v1.0',
    $PROMPT$
당신은 ETF 섹터 분석 전문가입니다. 주어진 ETF의 특정 섹터 구성을 분석하여
투자자에게 유용한 인사이트를 제공해주세요.

## 분석 원칙
1. 객관적 사실 기반 분석 (비중, 집중도, 종목 수)
2. 해당 섹터가 ETF 전체에서 갖는 의미
3. 잠재적 리스크와 기회 요인
4. 간결하고 명확한 문장 (2-3문장, 100자 내외)

## 분석 시 고려사항
- 상위 종목 집중도 (상위 2개 비중 > 70%면 높은 집중도)
- 섹터 비중 (전체 ETF에서 차지하는 비율)
- 종목 분산도 (종목 수와 HHI 지수)

반드시 아래 JSON 형식으로만 응답하세요:

{
  "analysis": "분석 결과 문장 (100자 내외, 한국어)",
  "risk_level": "LOW | MEDIUM | HIGH",
  "key_point": "핵심 포인트 한 단어 (예: 집중투자, 분산투자, 고성장, 안정성)"
}
$PROMPT$,
    '섹터 버블 AI 분석 v1.0 - 초기 버전',
    true
);
```

### 4.2 사용자 메시지 (User Message) 구조

```json
{
  "etf": {
    "name": "KODEX 200",
    "category": "시장형",
    "total_sectors": 18
  },
  "sector": {
    "group_code": "IT_SEMI",
    "group_name": "반도체",
    "weight_pct": 28.4,
    "stock_count": 12
  },
  "composition": {
    "top_stocks": [
      {"name": "삼성전자", "weight": 25.0},
      {"name": "SK하이닉스", "weight": 15.2},
      {"name": "한미반도체", "weight": 3.5},
      {"name": "리노공업", "weight": 2.1},
      {"name": "HPSP", "weight": 1.8}
    ],
    "top2_concentration": 80.2,
    "hhi": 0.092
  }
}
```

### 4.3 예상 AI 응답

```json
{
  "analysis": "반도체 섹터의 높은 기여도로 인해 IT 업황 회복 시 강한 반등이 예상됩니다. 단, 상위 2개 종목 비중이 80% 이상으로 집중도가 높은 점을 유의하세요.",
  "risk_level": "MEDIUM",
  "key_point": "집중투자"
}
```

---

## 5. 생성 로직

### 5.1 배치 처리 (권장)

```
┌─────────────────────────────────────────────────────────────┐
│                    배치 처리 흐름                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [스케줄러] 매일 새벽 3시                                    │
│       │                                                     │
│       ▼                                                     │
│  [1] etf_sector_cluster 조회                                │
│      - ai_analysis IS NULL 또는                             │
│      - ai_generated_at < base_date (데이터 갱신됨)          │
│       │                                                     │
│       ▼                                                     │
│  [2] 각 (ETF, 섹터) 조합별 데이터 수집                       │
│       │                                                     │
│       ▼                                                     │
│  [3] LLM API 호출 (rate limit 고려, 1초 간격)               │
│       │                                                     │
│       ▼                                                     │
│  [4] ai_analysis, ai_generated_at UPDATE                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 Python 배치 스크립트

```python
# backend/data-service/scripts/generate_sector_bubble_ai.py

import asyncio
import json
from datetime import datetime
from sqlalchemy import text
from app.database import get_db
from app.services.llm_service import LlmService
from app.config import settings

class SectorBubbleAIGenerator:
    """섹터 버블 AI 분석 생성기"""

    def __init__(self):
        self.llm = LlmService()
        self.prompt_name = "sector_bubble_analysis"

    async def generate_all(self, force: bool = False):
        """모든 섹터 버블에 대해 AI 분석 생성"""

        async with get_db() as db:
            # 1. 분석 필요한 섹터 버블 조회
            query = """
                SELECT
                    esc.id,
                    esc.etf_id,
                    esc.group_code,
                    esc.group_name,
                    esc.weight_pct,
                    esc.stock_count,
                    esc.base_date,
                    e.name as etf_name,
                    e.category as etf_category
                FROM etf_sector_cluster esc
                JOIN etf e ON e.id = esc.etf_id
                WHERE esc.cluster_type = 'GROUP_CODE'
                  AND (
                      esc.ai_analysis IS NULL
                      OR esc.ai_generated_at < esc.base_date
                      OR :force = true
                  )
                ORDER BY e.id, esc.weight_pct DESC
            """

            rows = await db.execute(text(query), {"force": force})
            clusters = rows.fetchall()

            print(f"[INFO] {len(clusters)}개 섹터 버블 AI 분석 생성 시작")

            # 2. 프롬프트 조회
            prompt = await self._get_prompt(db)

            # 3. 각 클러스터별 분석 생성
            for i, cluster in enumerate(clusters):
                try:
                    await self._generate_single(db, cluster, prompt)

                    if (i + 1) % 10 == 0:
                        print(f"[PROGRESS] {i + 1}/{len(clusters)} 완료")

                    # Rate limit 방지
                    await asyncio.sleep(0.5)

                except Exception as e:
                    print(f"[ERROR] cluster_id={cluster.id}: {e}")
                    continue

            await db.commit()
            print(f"[DONE] {len(clusters)}개 AI 분석 생성 완료")

    async def _get_prompt(self, db):
        """활성 프롬프트 조회"""
        query = """
            SELECT id, prompt_template
            FROM ai_prompt
            WHERE name = :name AND is_active = true
            LIMIT 1
        """
        result = await db.execute(text(query), {"name": self.prompt_name})
        return result.fetchone()

    async def _generate_single(self, db, cluster, prompt):
        """단일 섹터 버블 AI 분석 생성"""

        # 1. 상세 데이터 수집 (상위 종목, 집중도 등)
        context = await self._build_context(db, cluster)

        # 2. LLM 호출
        result = await self.llm.call_json(
            system_prompt=prompt.prompt_template,
            user_message=json.dumps(context, ensure_ascii=False),
            model="claude-sonnet-4-20250514"
        )

        # 3. 결과 저장
        update_query = """
            UPDATE etf_sector_cluster
            SET ai_analysis = :analysis,
                ai_generated_at = :generated_at,
                prompt_id = :prompt_id
            WHERE id = :id
        """

        await db.execute(text(update_query), {
            "id": cluster.id,
            "analysis": result.get("analysis", ""),
            "generated_at": datetime.now(),
            "prompt_id": prompt.id
        })

    async def _build_context(self, db, cluster) -> dict:
        """AI 분석용 컨텍스트 구성"""

        # 상위 5개 종목 조회
        top_stocks_query = """
            SELECT
                ci.company_name,
                esc.weight_pct
            FROM etf_stock_composition esc
            JOIN stock s ON s.id = esc.stock_id
            JOIN company_info ci ON ci.id = s.company_id
            JOIN industry_classification ic ON ic.code = ci.industry_code
            WHERE esc.etf_id = :etf_id
              AND ic.group_code = :group_code
              AND esc.base_date = :base_date
            ORDER BY esc.weight_pct DESC
            LIMIT 5
        """

        result = await db.execute(text(top_stocks_query), {
            "etf_id": cluster.etf_id,
            "group_code": cluster.group_code,
            "base_date": cluster.base_date
        })
        top_stocks = result.fetchall()

        # 집중도 계산
        weights = [s.weight_pct for s in top_stocks]
        top2_concentration = sum(weights[:2]) if len(weights) >= 2 else sum(weights)
        hhi = sum((w/100)**2 for w in weights)

        return {
            "etf": {
                "name": cluster.etf_name,
                "category": cluster.etf_category or "일반"
            },
            "sector": {
                "group_code": cluster.group_code,
                "group_name": cluster.group_name,
                "weight_pct": float(cluster.weight_pct),
                "stock_count": cluster.stock_count
            },
            "composition": {
                "top_stocks": [
                    {"name": s.company_name, "weight": float(s.weight_pct)}
                    for s in top_stocks
                ],
                "top2_concentration": round(top2_concentration, 1),
                "hhi": round(hhi, 4)
            }
        }


# 실행
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--force", action="store_true", help="모든 클러스터 재생성")
    args = parser.parse_args()

    generator = SectorBubbleAIGenerator()
    asyncio.run(generator.generate_all(force=args.force))
```

### 5.3 스케줄러 등록

```python
# backend/data-service/app/schedulers/scheduler.py 에 추가

from scripts.generate_sector_bubble_ai import SectorBubbleAIGenerator

@scheduler.scheduled_job('cron', hour=3, minute=30)
async def generate_sector_bubble_ai():
    """섹터 버블 AI 분석 생성 (매일 03:30)"""
    generator = SectorBubbleAIGenerator()
    await generator.generate_all()
```

---

## 6. API 응답

### 6.1 기존 API 확장

```
GET /api/v1/etf/{etfId}/sector-cluster
```

### 6.2 응답 예시

```json
{
  "success": true,
  "data": {
    "etfId": 871,
    "ticker": "069500",
    "name": "KODEX 200",
    "baseDate": "2025-01-31",
    "sectorCluster": [
      {
        "groupCode": "IT_SEMI",
        "groupName": "반도체",
        "weightPct": 28.4,
        "stockCount": 12,
        "posX": 0.5,
        "posY": 0.15,
        "radius": 0.08,
        "topStocks": [
          {"name": "삼성전자", "weight": 25.0},
          {"name": "SK하이닉스", "weight": 15.2}
        ],
        "aiAnalysis": {
          "analysis": "반도체 섹터의 높은 기여도로 인해 IT 업황 회복 시 강한 반등이 예상됩니다. 단, 상위 2개 종목 비중이 80% 이상으로 집중도가 높은 점을 유의하세요.",
          "riskLevel": "MEDIUM",
          "keyPoint": "집중투자"
        }
      },
      {
        "groupCode": "FINANCE",
        "groupName": "금융",
        "weightPct": 15.5,
        "stockCount": 15,
        "aiAnalysis": {
          "analysis": "금융 섹터는 은행, 증권, 보험이 고르게 분산되어 안정적입니다. 금리 환경 변화에 따른 수익성 변동에 주목하세요.",
          "riskLevel": "LOW",
          "keyPoint": "안정성"
        }
      }
    ]
  }
}
```

### 6.3 Java Entity 수정

```java
// EtfSectorCluster.java
@Entity
@Table(name = "etf_sector_cluster")
public class EtfSectorCluster {
    // ... 기존 필드들 ...

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    @Column(name = "ai_generated_at")
    private LocalDateTime aiGeneratedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private AiPrompt prompt;
}
```

### 6.4 Response DTO 수정

```java
// EtfSectorClusterResponse.java
@Getter
@Builder
public class SectorClusterItem {
    private String groupCode;
    private String groupName;
    private BigDecimal weightPct;
    private Integer stockCount;
    private Float posX;
    private Float posY;
    private Float radius;
    private List<TopStockDto> topStocks;

    // 신규 필드
    private SectorAiAnalysis aiAnalysis;

    @Getter
    @Builder
    public static class SectorAiAnalysis {
        private String analysis;      // "반도체 섹터의 높은 기여도로..."
        private String riskLevel;     // "LOW" | "MEDIUM" | "HIGH"
        private String keyPoint;      // "집중투자"
    }
}
```

---

## 7. 분석 품질 가이드라인

### 7.1 분석 유형별 템플릿

| 상황 | 분석 예시 |
|------|----------|
| **고집중 (top2 > 70%)** | "상위 2개 종목 비중이 {N}%로 집중도가 높습니다. 해당 종목의 변동성이 ETF 전체에 큰 영향을 줄 수 있습니다." |
| **저집중 (top2 < 40%)** | "{N}개 종목이 고르게 분산되어 개별 종목 리스크가 낮습니다." |
| **고비중 섹터 (> 25%)** | "전체 ETF의 {N}%를 차지하는 핵심 섹터입니다. 해당 산업 동향이 수익률에 직접적 영향을 줍니다." |
| **저비중 섹터 (< 5%)** | "비중이 {N}%로 낮아 ETF 전체 수익률에 미치는 영향은 제한적입니다." |

### 7.2 리스크 레벨 기준

| 레벨 | 조건 |
|------|------|
| **HIGH** | top2 집중도 > 80% 또는 HHI > 0.25 |
| **MEDIUM** | top2 집중도 50-80% 또는 HHI 0.1-0.25 |
| **LOW** | top2 집중도 < 50% 및 HHI < 0.1 |

---

## 8. 비용 및 성능 추정

### 8.1 예상 호출량

| 항목 | 값 |
|------|-----|
| ETF 수 | 173개 |
| ETF당 평균 섹터 수 | 8개 |
| 총 섹터 버블 수 | 약 1,400개 |
| 일일 갱신 대상 | 약 100개 (신규/변경분) |

### 8.2 비용 추정 (Claude Sonnet)

```
1회 호출당:
- 입력: ~500 토큰 (컨텍스트)
- 출력: ~100 토큰 (JSON 응답)
- 비용: 약 $0.002/회

일일 비용:
- 100회 × $0.002 = $0.2/일
- 월간: $6
```

### 8.3 처리 시간

```
- 1회 호출: ~1초
- 100개 처리: ~2분 (rate limit 포함)
- 전체 초기 생성: ~25분
```

---

## 9. 확장 계획

### 9.1 Phase 1 (현재)
- [x] GROUP_CODE 클러스터 AI 분석
- [ ] 배치 생성 스크립트
- [ ] API 응답 확장

### 9.2 Phase 2 (향후)
- SUB_SECTOR 클러스터 AI 분석 (테마 ETF용)
- 섹터 간 상관관계 분석
- 뉴스 연동 (최근 섹터 관련 뉴스 요약)

### 9.3 Phase 3 (고도화)
- 사용자 포트폴리오와 연계 ("내 포트폴리오와 비교 시...")
- 시계열 분석 ("지난달 대비 비중 변화...")
- 경쟁 ETF 비교 분석

---

## 10. 체크리스트

### 구현 순서

1. [ ] DB 스키마 확인/수정
   - `ai_generated_at` 컬럼 추가

2. [ ] ai_prompt 데이터 삽입
   - `sector_bubble_analysis` 프롬프트 INSERT

3. [ ] Python 배치 스크립트 작성
   - `generate_sector_bubble_ai.py`

4. [ ] 스케줄러 등록
   - 매일 03:30 실행

5. [ ] Java Entity/DTO 수정
   - `EtfSectorCluster.java`
   - `EtfSectorClusterResponse.java`

6. [ ] API 서비스 수정
   - `EtfServiceImpl.java` - aiAnalysis 포함 조회

7. [ ] 테스트
   - 단일 섹터 분석 생성
   - 전체 배치 실행
   - API 응답 확인

---

## 11. 관련 파일

```
backend/data-service/
├── scripts/
│   └── generate_sector_bubble_ai.py    # 배치 스크립트 (신규)
├── app/
│   ├── services/
│   │   └── llm_service.py              # LLM 호출 (기존)
│   └── schedulers/
│       └── scheduler.py                # 스케줄러 (수정)

backend/user-service/
├── src/main/java/.../domain/etf/
│   ├── entity/
│   │   └── EtfSectorCluster.java       # 엔티티 (수정)
│   ├── dto/
│   │   └── EtfSectorClusterResponse.java # DTO (수정)
│   └── service/impl/
│       └── EtfServiceImpl.java         # 서비스 (수정)

docs/
├── planning/
│   └── 섹터버블_AI분석_설계.md          # 본 문서
└── sql/
    └── sector_bubble_ai_prompt.sql     # 프롬프트 INSERT문
```
