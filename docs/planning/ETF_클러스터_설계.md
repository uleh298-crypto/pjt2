# ETF 섹터 분석 설계

> ETF 상세 페이지에서 구성종목의 섹터 분포를 버블 클러스터로 시각화하기 위한 설계 문서

---

## 1. 개요

### 1.1 목적

- **ETF 상세 페이지**에서 구성종목의 **산업/섹터 분포**를 **버블 클러스터**로 시각화
- 투자자가 "이 ETF가 어떤 산업에 투자하는지" 한눈에 파악
- ETF 유형에 따라 다른 분류 체계 적용

### 1.2 관계

```
ETF : 섹터 분포 시각화 = 1 : 1

하나의 ETF를 선택하면 → 해당 ETF의 구성종목 섹터 분포를 버블 클러스터로 표시
```

### 1.3 시각화 예시

```
              반도체 (28.4%)
                 ○
                 │
    화학 ○───────┼───────○ 금융
   (7.6%)        │      (15.5%)
                 │
           ┌─────────┐
           │  KODEX  │
           │   200   │
           └─────────┘
                 │
    자동차 ○─────┴─────○ 서비스
   (12.1%)            (8.2%)

※ 버블 크기 = weight_pct 비례
※ 버블 위치 = pos_x, pos_y 좌표
```

---

## 2. 분류 체계

> **현재 DB 데이터 기준** (2024년 기준)
> - `strategy_type`: 시장 대표, 테마형, 배당형, 채권형, 기타
> - `category`: 국내주식형 (현재 데이터는 국내주식형만 존재)
> - `sector`: 12개 넓은 카테고리 (전자 / IT, 금융, 자동차 등)

ETF의 `strategy_type`에 따라 다른 분류 방식을 적용합니다.

| strategy_type | sector 예시 | cluster_type | 버블 예시 |
|---------------|-------------|--------------|-----------|
| **시장 대표** | - | GROUP_CODE | 반도체(IT_SEMI), 금융(FINANCE), 바이오(BIO), 지주회사(HOLDING)... |
| **테마형** | 전자 / IT, 자동차 | SUB_SECTOR | 메모리(SEMI_MEM), 장비(SEMI_EQP), 2차전지(BATTERY)... |
| **테마형** | 금융, 바이오 / 의약 | INDUSTRY | 은행, 증권, 보험... |
| **배당형** | - | GROUP_CODE | 금융(FINANCE), 통신(TELECOM), 이벤트(EVENT)... |
| **기타** (파생형) | - | **ASSET_TYPE** | **선물, 채권, 현금, ETF, 우선주** |

> **group_code 목록 (21개 + 기타 3개)**:
> IT_SEMI, IT_ELEC, IT_SW, ENERGY, AUTO, BIO, CHEM, STEEL, MACHINERY, CONSTRUCT,
> FINANCE, INSURANCE, RETAIL, FOOD, CONSUMER, TELECOM, TRANSPORT, SHIPBUILD,
> DEFENSE, HOLDING, EVENT + 기타(AGRI, MINING, ETC)

> **Note**: 파생형 ETF는 `etf_other_composition` 테이블 기반으로 클러스터 생성.
> market_value 절대값 비율로 비중 계산 (weight가 0인 경우).

### 2.0 ETF 구성종목 테이블 구조

ETF 구성종목은 **2개 테이블**에 분산 저장됩니다:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ETF 구성종목 저장 구조                             │
├─────────────────────────────────────────────────────────────────────┤
│  etf_stock_composition                                              │
│  ├── 주식 기반 구성종목 (삼성전자, SK하이닉스 등)                      │
│  └── etf.sector 값이 있는 ETF의 주된 구성                            │
│                                                                     │
│  etf_other_composition                                              │
│  ├── 파생상품 구성종목 (선물, 채권, ETF 내 ETF, 우선주 등)             │
│  ├── asset_type: FUTURES, BOND, CASH, ETF, PREFERRED_STOCK          │
│  └── 인버스/레버리지/골드선물 ETF의 주된 구성                         │
└─────────────────────────────────────────────────────────────────────┘
```

#### ETF.sector 컬럼 의미

| sector 값 | 의미 | 구성종목 테이블 | 클러스터 방식 |
|-----------|------|-----------------|---------------|
| **값 있음** (전자 / IT, 금융, 자동차...) | 주식 기반 테마형 ETF | `etf_stock_composition` 위주 | SUB_SECTOR/INDUSTRY |
| **NULL/빈값** | 시장 대표/배당형/파생형 ETF | `etf_stock_composition` 또는 `etf_other_composition` | GROUP_CODE/ASSET_TYPE |

#### 현재 DB의 sector 값 (12개)

| sector | 대표 ETF |
|--------|---------|
| 전자 / IT | TIGER 반도체, KODEX AI반도체, TIGER 소프트웨어 |
| 금융 | TIGER 은행, KODEX 증권, TIGER 지주회사 |
| 자동차 | TIGER 2차전지, KODEX 자동차 |
| 바이오 / 의약 | TIGER 헬스케어, KODEX 바이오 |
| 에너지 / 유틸리티 | TIGER 신재생에너지, KODEX 기후변화솔루션 |
| 건설 | TIGER 200 건설, KODEX 건설 |
| 통신 / 미디어 | TIGER K게임, KODEX K콘텐츠 |
| 유통 / 소매 | TIGER 200 생활소비재 |
| 화학 / 소재 | TIGER 200 철강소재 |
| 철강 / 금속 | KODEX 철강 |
| 운송 | KODEX 운송 |
| 기타 | TIGER 삼성그룹, TIGER 모멘텀 |

#### 파생상품 ETF 예시 (12개)

```
| stock_code | ETF명                          | 구성 특성               |
|------------|--------------------------------|------------------------|
| 114800     | KODEX 인버스                   | KOSPI200 선물 (음수 비중) |
| 123310     | TIGER 인버스                   | KOSPI200 선물 (음수 비중) |
| 132030     | KODEX 골드선물(H)              | 금 선물 + USD 선물       |
| 319640     | TIGER 골드선물(H)              | 금 선물 + USD 선물       |
| 261140     | TIGER 우선주                   | 우선주 20종목           |
| 267770     | TIGER 200선물레버리지          | KOSPI200 선물 + ETF     |
```

**중요**: `etf.sector`가 NULL이어도 `etf_sector_cluster`에 클러스터 데이터가 있어야 합니다.
파생상품 ETF는 `cluster_type = 'ASSET_TYPE'`으로 선물/채권/현금 등의 분포를 표시합니다

### 2.1 cluster_type에 따른 집계 방식

#### 핵심 원리

`company_info.industry_code`에는 **Level 3 소분류**(표준 KSIC)를 입력합니다.
클러스터링 시 ETF 유형에 따라 **다른 수준으로 변환/집계**하여 버블 차트를 생성합니다.

> **역할 분담**
> - 팀원: `company_info.industry_code`에 소분류(Level 3) 매핑
> - 클러스터링 로직: 소분류 → 세분류(Level 4) 변환 (테마 ETF용)

```
company_info.industry_code (Level 3 소분류, 표준 KSIC)
         │
         ▼
industry_classification 테이블 JOIN
         │
         ├─── cluster_type = GROUP_CODE ──→ group_code로 집계 (21개 그룹 + 기타 3개)
         │
         ├─── cluster_type = SUB_SECTOR ──→ 소분류 → 세분류 변환 후 표시 (커스텀 매핑)
         │
         └─── cluster_type = INDUSTRY ───→ parent_code로 집계 (중분류)
```

#### 예시: 동일한 company_info 데이터로 다른 결과

**company_info + stock 테이블 (팀원이 입력 - Level 3 소분류, 공공데이터 API 형식):**
```
[stock 테이블]
┌──────────────┬──────────┬────────────┐
│ ticker       │ company_id│ market_type│
├──────────────┼──────────┼────────────┤
│ 005930       │ 1        │ KOSPI      │
│ 000660       │ 2        │ KOSPI      │
│ 042700       │ 3        │ KOSDAQ     │
└──────────────┴──────────┴────────────┘

[company_info 테이블]
┌──────────────┬──────────┬───────────────┐
│ id           │ 회사명    │ industry_code │
├──────────────┼──────────┼───────────────┤
│ 1            │ 삼성전자  │ C26100        │  ← 반도체 제조업
│ 2            │ SK하이닉스│ C26100        │  ← 반도체 제조업
│ 3            │ 한미반도체│ C26100        │  ← 반도체 제조업
│ 4            │ HPSP     │ C26100        │  ← 반도체 제조업
│ 5            │ NAVER    │ J63100        │  ← 정보서비스업
│ 6            │ KB금융   │ K64100        │  ← 은행 및 저축기관
└──────────────┴──────────┴───────────────┘
```

**industry_classification (seed.sql) - 공공데이터 API 형식:**
```
┌───────────┬─────────────────┬───────┬────────────┬──────────────┐
│ code      │ name            │ level │ group_code │ parent_code  │
├───────────┼─────────────────┼───────┼────────────┼──────────────┤
│ C26100    │ 반도체 제조업    │ 3     │ IT_SEMI    │ 26           │
│ C26200    │ 전자부품 제조업  │ 3     │ IT_ELEC    │ 26           │
│ SEMI_MEM  │ 메모리 반도체    │ 4     │ IT_SEMI    │ C26100       │  ← 커스텀 세분류
│ SEMI_HBM  │ HBM             │ 4     │ IT_SEMI    │ C26100       │  ← 커스텀 세분류
│ SEMI_EQP  │ 반도체 장비      │ 4     │ IT_SEMI    │ C26100       │  ← 커스텀 세분류
│ J63100    │ 정보서비스업     │ 3     │ IT_SW      │ 63           │
│ K64100    │ 은행 및 저축기관 │ 3     │ FINANCE    │ 64           │
└───────────┴─────────────────┴───────┴────────────┴──────────────┘
```

> **SUB_SECTOR 클러스터링 시**: 소분류(C26100) → 세분류(SEMI_MEM, SEMI_HBM) 변환
> **변환 방식**: 하이브리드 (수동 매핑 테이블 + 기본 매핑)

#### ETF 구성종목 → 섹터 매핑 (클러스터 태그용)

```
┌─────────────────────────────────────────────────────────────────┐
│                    클러스터 매핑 흐름                             │
├─────────────────────────────────────────────────────────────────┤
│  etf_stock_composition (ETF 구성종목)                            │
│         │                                                       │
│         ▼                                                       │
│  etf_stock_cluster_mapping (섹터 매핑)                           │
│  ├── etf_id                                                     │
│  ├── composition_id (FK → etf_stock_composition)                │
│  ├── sector_code (FK → industry_classification Level 4)         │
│  └── source (MANUAL / AI)                                       │
│                                                                 │
│  예시:                                                          │
│  ├─ 반도체ETF + 삼성전자 → sector_code: SEMI_MEM                 │
│  ├─ 반도체ETF + SK하이닉스 → sector_code: SEMI_HBM               │
│  └─ 모바일ETF + 삼성전자 → sector_code: MOBILE                   │
│                                                                 │
│  ※ 같은 회사도 ETF에 따라 다른 섹터로 매핑 가능                    │
└─────────────────────────────────────────────────────────────────┘
```

**관련 테이블:**
- `etf_stock_cluster_mapping`: ETF 주식 구성종목의 섹터 매핑
- `etf_other_cluster_mapping`: ETF 비주식 구성종목(선물/채권)의 섹터 매핑

---

#### Case 1: KODEX 200 (시장 대표) → cluster_type = GROUP_CODE

**group_code로 집계 → 큰 산업군으로 묶음**

```
┌────────────┬────────────┬────────┐
│ group_code │ group_name │ 비중   │
├────────────┼────────────┼────────┤
│ IT_SEMI    │ 반도체     │ 55%   │  ← 삼성전자+SK하이닉스+한미반도체+HPSP
│ IT_SW      │ 소프트웨어 │ 15%   │  ← NAVER
│ FINANCE    │ 금융       │ 20%   │  ← KB금융
└────────────┴────────────┴────────┘

버블 차트:
        반도체 (55%)
           ●

   소프트웨어 ●     ● 금융
     (15%)         (20%)
```

---

#### Case 2: TIGER 반도체 (테마형, sector=전자 / IT) → cluster_type = SUB_SECTOR

**소분류 → 세분류 변환 후 표시 → 반도체 내 세부 분류**

```
[변환 과정]
etf_stock_cluster_mapping (ETF별 섹터 매핑)
┌──────────┬──────────────┬─────────────────┐
│ etf_id   │ composition_id│ sector_code     │
├──────────┼──────────────┼─────────────────┤
│ 1 (반도체)│ 101 (삼성전자)│ SEMI_MEM        │  ← 메모리 반도체
│ 1 (반도체)│ 102 (SK하닉) │ SEMI_HBM        │  ← HBM
│ 1 (반도체)│ 103 (한미반) │ SEMI_EQP        │  ← 반도체 장비
│ 1 (반도체)│ 104 (솔브레인)│ SEMI_MAT        │  ← 반도체 소재
└──────────┴──────────────┴─────────────────┘
※ 변환 로직: etf_stock_cluster_mapping 테이블에서 ETF별 섹터 매핑 조회

[집계 결과]
┌───────────────┬─────────────────┬────────┐
│ sub_sector    │ name            │ 비중   │
├───────────────┼─────────────────┼────────┤
│ SEMI_MEM      │ 메모리 반도체    │ 30%   │
│ SEMI_HBM      │ HBM             │ 25%   │
│ SEMI_EQP      │ 반도체 장비      │ 18%   │
└───────────────┴─────────────────┴────────┘

버블 차트:
        메모리 (30%)
           ●

      HBM ●     ● 장비
    (25%)       (18%)
```

---

#### Case 3: TIGER 은행 (테마형, sector=금융) → cluster_type = INDUSTRY

**parent_code(중분류)로 집계 → 금융 내 세부 업종**

```
┌──────────────┬────────────────┬────────┐
│ parent_code  │ name           │ 비중   │
├──────────────┼────────────────┼────────┤
│ 116401       │ 은행           │ 40%   │
│ 116402       │ 증권/투자      │ 30%   │
│ 116501       │ 보험           │ 20%   │
└──────────────┴────────────────┴────────┘

버블 차트:
        은행 (40%)
           ●

      증권 ●     ● 보험
    (30%)       (20%)
```

---

### 2.2 cluster_type 결정 로직

`etf` 테이블의 2가지 필드를 사용하여 cluster_type을 결정합니다:

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ETF 테이블 필드                               │
├─────────────────┬───────────────────────────────────────────────────┤
│ strategy_type   │ 시장 대표, 테마형, 배당형, 채권형, 기타             │
│                 │ → cluster_type 결정의 주요 기준                    │
├─────────────────┼───────────────────────────────────────────────────┤
│ sector          │ 전자 / IT, 금융, 자동차, 바이오 / 의약 등 (12개)    │
│                 │ → 테마형 ETF의 세부 분류 (테마형일 때만 사용)        │
├─────────────────┼───────────────────────────────────────────────────┤
│ category        │ 국내주식형, 해외주식형, 채권형, 원자재형, 통화형     │
│                 │ → 자산 유형 분류                                   │
└─────────────────┴───────────────────────────────────────────────────┘
```

#### 결정 로직

```python
def determine_cluster_type(etf) -> str | None:
    """ETF 유형에 따라 cluster_type 결정"""

    # 시장 대표 ETF (KODEX 200, KOSPI 등) → 22개 그룹코드로 집계
    if etf.strategy_type == "시장 대표":
        return "GROUP_CODE"

    # 테마형 ETF → sector에 따라 다른 집계 방식
    elif etf.strategy_type == "테마형":
        # 세분류가 의미있는 섹터 → SUB_SECTOR로 상세 표시
        sub_sector_types = ["전자 / IT", "자동차", "에너지 / 유틸리티"]
        if etf.sector in sub_sector_types:
            return "SUB_SECTOR"

        # 중분류가 의미있는 섹터 → INDUSTRY로 표시
        industry_types = ["금융", "바이오 / 의약"]
        if etf.sector in industry_types:
            return "INDUSTRY"

        # 기타 테마 → 기본 GROUP_CODE
        return "GROUP_CODE"

    # 배당형 ETF → 어떤 산업군에서 배당을 주는지
    elif etf.strategy_type == "배당형":
        return "GROUP_CODE"

    # 채권형, 기타 (파생형) → ASSET_TYPE 또는 제외
    elif etf.strategy_type == "기타":
        return "ASSET_TYPE"  # 파생상품 ETF

    else:
        return None  # 채권형 등
```

#### 적용 예시

| ETF | strategy_type | sector | cluster_type | 버블 표시 |
|-----|---------------|--------|--------------|-----------|
| KODEX 200 | 시장 대표 | - | GROUP_CODE | 반도체, 금융, 바이오... |
| TIGER 반도체 | 테마형 | 전자 / IT | SUB_SECTOR | 메모리, 장비, 팹리스... |
| TIGER 2차전지 | 테마형 | 자동차 | SUB_SECTOR | 배터리셀, 소재, 장비... |
| KODEX 은행 | 테마형 | 금융 | INDUSTRY | 은행, 증권, 보험... |
| TIGER 헬스케어 | 테마형 | 바이오 / 의약 | INDUSTRY | 제약, 바이오, 의료기기... |
| KODEX 고배당 | 배당형 | - | GROUP_CODE | 금융, 통신, 유틸리티... |
| KODEX 인버스 | 기타 | - | ASSET_TYPE | 선물, 채권, 현금... |

### 2.3 집계 쿼리 예시

```sql
-- GROUP_CODE 집계 (시장 대표/배당형 ETF)
SELECT
    ic.group_code,
    ic.group_name,
    SUM(esc.weight_pct) as total_weight,
    COUNT(*) as stock_count
FROM etf_stock_composition esc
JOIN stock s ON s.id = esc.stock_id
JOIN company_info ci ON ci.id = s.company_id
JOIN industry_classification ic ON ic.code = ci.industry_code
WHERE esc.etf_id = :etf_id
GROUP BY ic.group_code, ic.group_name
ORDER BY total_weight DESC;

-- SUB_SECTOR 집계 (테마형 ETF: 전자 / IT, 자동차, 에너지 / 유틸리티)
-- etf_stock_cluster_mapping 테이블 사용
SELECT
    escm.sector_code as sub_sector,
    ic.name as sub_sector_name,
    SUM(esc.weight_pct) as total_weight,
    COUNT(*) as stock_count
FROM etf_stock_cluster_mapping escm
JOIN etf_stock_composition esc ON esc.id = escm.composition_id
JOIN industry_classification ic ON ic.code = escm.sector_code
WHERE escm.etf_id = :etf_id
GROUP BY escm.sector_code, ic.name
ORDER BY total_weight DESC;

-- INDUSTRY 집계 (테마형 ETF: 금융, 바이오 / 의약)
SELECT
    ic.parent_code as industry_code,
    parent_ic.name as industry_name,
    SUM(esc.weight_pct) as total_weight,
    COUNT(*) as stock_count
FROM etf_stock_composition esc
JOIN stock s ON s.id = esc.stock_id
JOIN company_info ci ON ci.id = s.company_id
JOIN industry_classification ic ON ic.code = ci.industry_code
JOIN industry_classification parent_ic ON parent_ic.code = ic.parent_code
WHERE esc.etf_id = :etf_id
GROUP BY ic.parent_code, parent_ic.name
ORDER BY total_weight DESC;
```

---

## 3. 테이블 구조

### 3.1 테이블 관계

```
┌─────────────────────────────┐
│ etf_stock_cluster_mapping   │  개별 구성종목 → 섹터 매핑 (원본)
├─────────────────────────────┤
│ composition_id (FK)         │  ← etf_stock_composition
│ sector_code                 │  ← industry_classification (Level 4)
└──────────────┬──────────────┘
               │ GROUP BY sector_code
               ▼
┌─────────────────────────────┐
│ etf_sector_cluster          │  섹터별 집계 + 시각화 좌표 (결과)
├─────────────────────────────┤
│ sub_sector / group_code     │
│ weight_pct (합계)           │
│ stock_count (개수)          │
│ pos_x, pos_y, radius        │  ← 버블 차트 좌표
└─────────────────────────────┘
```

### 3.2 etf_sector_cluster

```sql
CREATE TABLE "etf_sector_cluster" (
    "id" BIGSERIAL PRIMARY KEY,
    "etf_id" BIGINT NOT NULL,

    -- 분류 타입
    "cluster_type" VARCHAR(20) NOT NULL,    -- 'GROUP_CODE' / 'INDUSTRY' / 'SUB_SECTOR'

    -- 산업분류 (KSIC 기반)
    "industry_code" VARCHAR(10),              -- KSIC 코드 (C26, K64 등)
    "industry_name" VARCHAR(100),             -- 산업명 (전자부품, 금융업 등)

    -- 그룹코드 (UI용 13개 그룹)
    "group_code" VARCHAR(20),                 -- IT_SW, FINANCE, ENERGY 등
    "group_name" VARCHAR(50),                 -- 정보통신, 금융, 에너지 등

    -- 세부 섹터 (테마 ETF용)
    "sub_sector" VARCHAR(100),                -- 세라믹, 공정장비, 팹리스 등

    -- 비중 정보
    "weight_pct" DECIMAL(6,3) NOT NULL,       -- 비중 (%)
    "stock_count" INTEGER,                    -- 해당 섹터 종목 수

    -- 시각화 좌표 (UMAP)
    "pos_x" DECIMAL(10,6),                    -- 버블 X 좌표
    "pos_y" DECIMAL(10,6),                    -- 버블 Y 좌표
    "radius" DECIMAL(10,6),                   -- 버블 반지름
    "distance_to_center" DECIMAL(10,6),       -- ETF 중심까지 거리

    "base_date" DATE NOT NULL,                -- 기준일
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_sector_cluster_etf" FOREIGN KEY ("etf_id")
        REFERENCES "etf"("id") ON DELETE CASCADE
);

CREATE INDEX "idx_sector_cluster_etf" ON "etf_sector_cluster"("etf_id");
CREATE INDEX "idx_sector_cluster_date" ON "etf_sector_cluster"("etf_id", "base_date" DESC);
```

### 3.2 컬럼 설명

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| etf_id | BIGINT | ETF FK |
| cluster_type | VARCHAR(20) | 분류 타입: GROUP_CODE / INDUSTRY / SUB_SECTOR |
| industry_code | VARCHAR(10) | KSIC 산업코드 |
| industry_name | VARCHAR(100) | 산업명 |
| group_code | VARCHAR(20) | 그룹코드 (13개) |
| group_name | VARCHAR(50) | 그룹명 |
| sub_sector | VARCHAR(100) | 세부 섹터명 (테마 ETF용) |
| weight_pct | DECIMAL(6,3) | 비중 (%) |
| stock_count | INTEGER | 해당 섹터 종목 수 |
| **pos_x** | DECIMAL(10,6) | 버블 X 좌표 (정규화: 0.0 ~ 1.0) |
| **pos_y** | DECIMAL(10,6) | 버블 Y 좌표 (정규화: 0.0 ~ 1.0) |
| **radius** | DECIMAL(10,6) | 버블 반지름 (정규화) |
| **distance_to_center** | DECIMAL(10,6) | ETF 중심(0.5, 0.5)까지 거리 |
| base_date | DATE | 기준일 |

---

## 4. 좌표 체계

### 4.1 좌표 정규화

모든 좌표는 **0.0 ~ 1.0** 범위로 정규화됩니다.

```
(0,0) ─────────────────────────── (1,0)
  │                                 │
  │         (0.5, 0.1)              │
  │            ○ 반도체             │
  │                                 │
  │  (0.1,0.5)     (0.5,0.5)       │
  │    ○ ─────────── ● ─────── ○   │
  │   화학          중심       금융 │
  │                (ETF)     (0.9,0.5)
  │                                 │
  │         (0.5, 0.9)              │
  │            ○ 서비스             │
  │                                 │
(0,1) ─────────────────────────── (1,1)
```

### 4.2 좌표 계산 (백엔드 배치)

```python
import numpy as np

def calculate_sector_positions(sectors: list) -> list:
    """섹터별 버블 위치 계산"""

    n = len(sectors)
    center = (0.5, 0.5)
    base_distance = 0.35  # 기본 거리

    for i, sector in enumerate(sectors):
        # 각도 계산 (균등 배치)
        angle = (2 * np.pi * i / n) - (np.pi / 2)  # 12시 방향부터 시작

        # 거리 (비중 클수록 중심에 가깝게)
        weight_factor = sector['weight_pct'] / 100
        distance = base_distance * (1 - weight_factor * 0.3)

        # 좌표 계산
        sector['pos_x'] = center[0] + distance * np.cos(angle)
        sector['pos_y'] = center[1] + distance * np.sin(angle)

        # 반지름 (비중 비례)
        sector['radius'] = 0.03 + (weight_factor * 0.12)

        # 중심까지 거리
        sector['distance_to_center'] = distance

    return sectors
```

---

## 5. API 응답 예시

### 5.1 ETF 섹터 분포 조회

```json
GET /api/v1/etf/123/sector-cluster

{
  "etf_id": 123,
  "etf_name": "KODEX 200",
  "cluster_type": "GROUP_CODE",
  "base_date": "2024-01-15",
  "center": {
    "x": 0.5,
    "y": 0.5
  },
  "sectors": [
    {
      "group_code": "IT_SEMI",
      "group_name": "반도체",
      "weight_pct": 28.4,
      "stock_count": 15,
      "pos_x": 0.5,
      "pos_y": 0.15,
      "radius": 0.064,
      "distance_to_center": 0.35
    },
    {
      "group_code": "FINANCE",
      "group_name": "금융",
      "weight_pct": 15.5,
      "stock_count": 12,
      "pos_x": 0.85,
      "pos_y": 0.5,
      "radius": 0.049,
      "distance_to_center": 0.35
    },
    {
      "group_code": "AUTO",
      "group_name": "자동차",
      "weight_pct": 12.1,
      "stock_count": 8,
      "pos_x": 0.25,
      "pos_y": 0.8,
      "radius": 0.044,
      "distance_to_center": 0.38
    }
  ]
}
```

---

## 6. Android Compose 렌더링 가이드

### 6.1 의존성

```kotlin
// build.gradle.kts
dependencies {
    // Compose BOM으로 버전 통일 관리
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-graphics")
}
```

### 6.2 데이터 모델

```kotlin
data class SectorBubble(
    val name: String,
    val weightPct: Float,
    val posX: Float,        // 0.0 ~ 1.0
    val posY: Float,        // 0.0 ~ 1.0
    val radius: Float,      // 정규화된 반지름
    val distanceToCenter: Float,
    val color: Color
)

data class EtfClusterData(
    val etfName: String,
    val sectors: List<SectorBubble>
)
```

### 6.3 버블 클러스터 Composable

```kotlin
@Composable
fun EtfSectorCluster(
    data: EtfClusterData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)  // 정사각형 유지
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 중심 ETF 원 그리기
            val centerX = canvasWidth * 0.5f
            val centerY = canvasHeight * 0.5f
            val centerRadius = canvasWidth * 0.15f

            drawCircle(
                color = Color(0xFF4CAF50),
                radius = centerRadius,
                center = Offset(centerX, centerY)
            )

            // 섹터 버블들 그리기
            data.sectors.forEach { sector ->
                val bubbleX = canvasWidth * sector.posX
                val bubbleY = canvasHeight * sector.posY
                val bubbleRadius = canvasWidth * sector.radius

                // 버블 원
                drawCircle(
                    color = sector.color.copy(alpha = 0.3f),
                    radius = bubbleRadius,
                    center = Offset(bubbleX, bubbleY)
                )

                // 버블 테두리
                drawCircle(
                    color = sector.color,
                    radius = bubbleRadius,
                    center = Offset(bubbleX, bubbleY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // 중심 ETF 라벨
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.etfName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // 섹터 라벨들
        data.sectors.forEach { sector ->
            SectorLabel(
                sector = sector,
                canvasSize = with(LocalDensity.current) {
                    // Canvas 크기에 맞춰 위치 계산
                }
            )
        }
    }
}

@Composable
fun SectorLabel(
    sector: SectorBubble,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 섹터 아이콘
        Icon(
            imageVector = getSectorIcon(sector.name),
            contentDescription = sector.name,
            tint = sector.color,
            modifier = Modifier.size(24.dp)
        )

        // 섹터명
        Text(
            text = sector.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )

        // 비중
        Text(
            text = "${sector.weightPct}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
```

### 6.4 전체 화면 구성

```kotlin
@Composable
fun EtfDetailScreen(
    etfId: Long,
    viewModel: EtfDetailViewModel = hiltViewModel()
) {
    val clusterData by viewModel.clusterData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 탭 메뉴
        TabRow(selectedTabIndex = 0) {
            Tab(selected = true, onClick = {}) {
                Text("클러스터", modifier = Modifier.padding(16.dp))
            }
            Tab(selected = false, onClick = {}) {
                Text("ETF 상세보기", modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ETF 이름
        Text(
            text = clusterData?.etfName ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "KOSPI 200 Index Tracking Fund",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 클러스터 시각화
        clusterData?.let { data ->
            EtfSectorCluster(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
```

### 6.5 좌표 변환 유틸리티

```kotlin
object ClusterCoordinateUtils {

    /**
     * 정규화된 좌표(0~1)를 Canvas 픽셀 좌표로 변환
     */
    fun normalizedToPixel(
        normalizedX: Float,
        normalizedY: Float,
        canvasWidth: Float,
        canvasHeight: Float
    ): Offset {
        return Offset(
            x = normalizedX * canvasWidth,
            y = normalizedY * canvasHeight
        )
    }

    /**
     * 정규화된 반지름을 픽셀 반지름으로 변환
     */
    fun normalizedRadiusToPixel(
        normalizedRadius: Float,
        canvasWidth: Float
    ): Float {
        return normalizedRadius * canvasWidth
    }

    /**
     * 섹터별 색상 반환 (21개 그룹 + 기타 3개)
     */
    fun getSectorColor(groupCode: String): Color {
        return when (groupCode) {
            // IT/기술
            "IT_SEMI" -> Color(0xFF2196F3)     // 반도체 - 파랑
            "IT_SW" -> Color(0xFF03A9F4)       // 소프트웨어 - 하늘
            "IT_ELEC" -> Color(0xFF00BCD4)     // 전자 - 청록
            // 금융
            "FINANCE" -> Color(0xFF4CAF50)    // 금융 - 초록
            "INSURANCE" -> Color(0xFF81C784)  // 보험 - 연초록
            // 제조/산업재
            "AUTO" -> Color(0xFF795548)       // 자동차 - 갈색
            "CHEM" -> Color(0xFFFF5722)       // 화학 - 주황빨강
            "STEEL" -> Color(0xFF64748B)      // 철강 - 슬레이트
            "MACHINERY" -> Color(0xFFBDBDBD)  // 기계 - 회색
            "SHIPBUILD" -> Color(0xFF06B6D4)  // 조선 - 시안
            "CONSTRUCT" -> Color(0xFF78716C)  // 건설 - 갈색
            // 에너지/바이오
            "ENERGY" -> Color(0xFF9C27B0)     // 에너지 - 보라
            "BIO" -> Color(0xFF009688)        // 바이오 - 청록
            // 소비재/서비스
            "CONSUMER" -> Color(0xFFE91E63)   // 소비재 - 분홍
            "RETAIL" -> Color(0xFFFF4081)     // 유통 - 핑크
            "FOOD" -> Color(0xFFFFB74D)       // 식품 - 주황
            // 인프라/통신
            "TELECOM" -> Color(0xFF673AB7)    // 통신 - 보라
            "TRANSPORT" -> Color(0xFF26A69A)  // 운송 - 청록
            "DEFENSE" -> Color(0xFF546E7A)    // 방위 - 청회색
            // 지주/이벤트
            "HOLDING" -> Color(0xFFA855F7)    // 지주회사 - 퍼플
            "EVENT" -> Color(0xFF9CA3AF)      // 이벤트 - 회색
            // 기타
            else -> Color(0xFF9E9E9E)
        }
    }
}
```

### 6.6 섹터 아이콘 매핑

```kotlin
@Composable
fun getSectorIcon(sectorName: String): ImageVector {
    return when {
        sectorName.contains("반도체") -> Icons.Default.Memory
        sectorName.contains("금융") -> Icons.Default.AccountBalance
        sectorName.contains("자동차") -> Icons.Default.DirectionsCar
        sectorName.contains("화학") -> Icons.Default.Science
        sectorName.contains("서비스") -> Icons.Default.Storefront
        sectorName.contains("에너지") -> Icons.Default.ElectricBolt
        sectorName.contains("헬스") -> Icons.Default.LocalHospital
        else -> Icons.Default.Category
    }
}
```

---

## 7. 데이터 흐름

```
┌─────────────────────────┐
│ etf_stock_composition   │  구성종목 (삼성전자 15%, SK하이닉스 10%...)
└────────┬────────────────┘
         │
         ▼ JOIN
┌──────────────────┐
│      stock       │  주식 → 회사 연결
└────────┬─────────┘
         │
         ▼ JOIN
┌──────────────────┐
│   company_info   │  종목 → 산업코드 매핑
└────────┬─────────┘
         │
         ├─── [시장 대표/배당형 ETF] ───────────────────────────┐
         │                                                      │
         ▼ JOIN                                                 │
┌──────────────────────┐                                       │
│ industry_classification │  산업코드 → group_code 매핑         │
└────────┬────────────────┘                                    │
         │                                                      │
         │                                                      │
┌────────┴─── [테마형 ETF] ────────────────────────────────────┼───┐
│                                                            │   │
▼                                                            │   │
┌─────────────────────────────┐                              │   │
│ etf_stock_cluster_mapping   │  구성종목별 세분류 매핑        │   │
└────────┬────────────────────┘                              │   │
         │                                                    │   │
         ▼ 집계 + 좌표계산 ◄──────────────────────────────────┘   │
┌──────────────────────┐                                         │
│ etf_sector_cluster   │  ETF별 섹터 분포 + 좌표 저장             │
└────────┬─────────────┘                                         │
         │                                                        │
         ▼ API ◄─────────────────────────────────────────────────┘
┌──────────────────────┐
│   Android Compose    │  버블 클러스터 렌더링
└──────────────────────┘
```

---

## 8. 배치 작업

### 8.1 섹터 분석 배치 (일 1회)

```python
async def update_etf_sector_cluster():
    """ETF별 구성종목 섹터 분포 집계 + 좌표 계산"""

    for etf in get_active_etfs():
        # 1. 구성종목 조회
        compositions = get_compositions(etf.id)

        # 2. ETF 유형에 따라 cluster_type 결정
        cluster_type = determine_cluster_type(etf)

        # 3. 종목 → 산업/섹터 매핑 및 집계
        sectors = aggregate_by_sector(compositions, cluster_type)

        # 4. 좌표 계산 (UMAP 또는 원형 배치)
        sectors = calculate_sector_positions(sectors)

        # 5. 기존 데이터 삭제 후 새로 저장
        delete_old_cluster(etf.id)
        save_sector_cluster(etf.id, cluster_type, sectors)
```

---

## 9. 테이블 관계도

```
┌─────────────┐     1:N      ┌─────────────────────┐
│     etf     │ ◄─────────── │  etf_sector_cluster│
├─────────────┤              ├─────────────────────┤
│ id (PK)     │              │ etf_id (FK)         │
│ name        │              │ cluster_type      │
│ category    │              │ group_code          │
│ ...         │              │ sub_sector          │
└─────────────┘              │ weight_pct          │
                             │ pos_x, pos_y        │
                             │ radius              │
                             │ distance_to_center  │
                             └─────────────────────┘
```

---

## 10. Android Compose 상세 구현 가이드

### 10.1 API Response 모델

```kotlin
// dto/EtfSectorClusterResponse.kt
data class EtfSectorClusterResponse(
    @SerializedName("etf_id") val etfId: Long,
    @SerializedName("etf_name") val etfName: String,
    @SerializedName("cluster_type") val clusterType: String,
    @SerializedName("base_date") val baseDate: String,
    val center: CenterPoint,
    val sectors: List<SectorItem>
)

data class CenterPoint(
    val x: Float,
    val y: Float
)

data class SectorItem(
    @SerializedName("group_code") val groupCode: String?,
    @SerializedName("group_name") val groupName: String?,
    @SerializedName("sub_sector") val subSector: String?,
    @SerializedName("weight_pct") val weightPct: Float,
    @SerializedName("stock_count") val stockCount: Int,
    @SerializedName("pos_x") val posX: Float,
    @SerializedName("pos_y") val posY: Float,
    val radius: Float,
    @SerializedName("distance_to_center") val distanceToCenter: Float
) {
    // 표시용 이름 (group_name 또는 sub_sector)
    val displayName: String
        get() = groupName ?: subSector ?: "기타"
}
```

### 10.2 UI State 모델

```kotlin
// ui/state/EtfClusterUiState.kt
sealed interface EtfClusterUiState {
    object Loading : EtfClusterUiState
    data class Success(val data: EtfClusterData) : EtfClusterUiState
    data class Error(val message: String) : EtfClusterUiState
}

data class EtfClusterData(
    val etfId: Long,
    val etfName: String,
    val clusterType: String,
    val baseDate: String,
    val sectors: List<SectorBubble>,
    val selectedSector: SectorBubble? = null
)

data class SectorBubble(
    val id: String,           // groupCode 또는 subSector
    val name: String,
    val weightPct: Float,
    val stockCount: Int,
    val posX: Float,          // 0.0 ~ 1.0
    val posY: Float,          // 0.0 ~ 1.0
    val radius: Float,
    val distanceToCenter: Float,
    val color: Color
)
```

### 10.3 ViewModel 구현

```kotlin
// viewmodel/EtfClusterViewModel.kt
@HiltViewModel
class EtfClusterViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val etfId: Long = savedStateHandle.get<Long>("etfId") ?: 0L

    private val _uiState = MutableStateFlow<EtfClusterUiState>(EtfClusterUiState.Loading)
    val uiState: StateFlow<EtfClusterUiState> = _uiState.asStateFlow()

    init {
        loadClusterData()
    }

    fun loadClusterData() {
        viewModelScope.launch {
            _uiState.value = EtfClusterUiState.Loading

            etfRepository.getEtfSectorCluster(etfId)
                .onSuccess { response ->
                    _uiState.value = EtfClusterUiState.Success(
                        data = response.toUiModel()
                    )
                }
                .onFailure { error ->
                    _uiState.value = EtfClusterUiState.Error(
                        message = error.message ?: "데이터를 불러올 수 없습니다"
                    )
                }
        }
    }

    fun onSectorClicked(sector: SectorBubble) {
        val currentState = _uiState.value
        if (currentState is EtfClusterUiState.Success) {
            _uiState.value = currentState.copy(
                data = currentState.data.copy(
                    selectedSector = if (currentState.data.selectedSector?.id == sector.id) {
                        null  // 같은 섹터 다시 클릭하면 선택 해제
                    } else {
                        sector
                    }
                )
            )
        }
    }

    fun clearSelection() {
        val currentState = _uiState.value
        if (currentState is EtfClusterUiState.Success) {
            _uiState.value = currentState.copy(
                data = currentState.data.copy(selectedSector = null)
            )
        }
    }
}

// Response → UiModel 변환
private fun EtfSectorClusterResponse.toUiModel(): EtfClusterData {
    return EtfClusterData(
        etfId = etfId,
        etfName = etfName,
        clusterType = clusterType,
        baseDate = baseDate,
        sectors = sectors.map { sector ->
            SectorBubble(
                id = sector.groupCode ?: sector.subSector ?: "",
                name = sector.displayName,
                weightPct = sector.weightPct,
                stockCount = sector.stockCount,
                posX = sector.posX,
                posY = sector.posY,
                radius = sector.radius,
                distanceToCenter = sector.distanceToCenter,
                color = ClusterColors.getSectorColor(sector.groupCode ?: "")
            )
        }
    )
}
```

### 10.4 색상 및 상수 정의

```kotlin
// ui/theme/ClusterColors.kt
object ClusterColors {
    val CenterGreen = Color(0xFF4CAF50)
    val CenterGreenDark = Color(0xFF388E3C)

    // 21개 주요 그룹 + 기타 3개 (AGRI, MINING, ETC)
    private val sectorColors = mapOf(
        // IT/기술
        "IT_SEMI" to Color(0xFF2196F3),      // 반도체 - 파랑
        "IT_SW" to Color(0xFF03A9F4),        // 소프트웨어 - 하늘
        "IT_ELEC" to Color(0xFF00BCD4),      // 전자/IT - 청록

        // 금융
        "FINANCE" to Color(0xFF4CAF50),      // 금융 - 초록
        "INSURANCE" to Color(0xFF81C784),    // 보험 - 연초록

        // 제조/산업재
        "AUTO" to Color(0xFF795548),         // 자동차 - 갈색
        "CHEM" to Color(0xFFFF5722),         // 화학/소재 - 주황빨강
        "STEEL" to Color(0xFF64748B),        // 철강/금속 - 슬레이트
        "MACHINERY" to Color(0xFFBDBDBD),    // 기계/로봇 - 회색
        "SHIPBUILD" to Color(0xFF06B6D4),    // 조선 - 시안
        "CONSTRUCT" to Color(0xFF78716C),    // 건설 - 갈색

        // 에너지/바이오
        "ENERGY" to Color(0xFF9C27B0),       // 에너지 - 보라
        "BIO" to Color(0xFF009688),          // 바이오/의료 - 청록

        // 소비재/서비스
        "CONSUMER" to Color(0xFFE91E63),     // 소비재 - 분홍
        "RETAIL" to Color(0xFFFF4081),       // 유통 - 핑크
        "FOOD" to Color(0xFFFFB74D),         // 식품 - 주황

        // 인프라/통신
        "TELECOM" to Color(0xFF673AB7),      // 통신/미디어 - 보라
        "TRANSPORT" to Color(0xFF26A69A),    // 운송 - 청록
        "DEFENSE" to Color(0xFF546E7A),      // 방위/우주 - 청회색

        // 지주/이벤트
        "HOLDING" to Color(0xFFA855F7),      // 지주회사 - 퍼플
        "EVENT" to Color(0xFF9CA3AF),        // 이벤트/테마 - 회색

        // 기타
        "AGRI" to Color(0xFF8BC34A),         // 농업/어업 - 연두
        "MINING" to Color(0xFF8D6E63),       // 광업 - 갈색
        "ETC" to Color(0xFF9E9E9E)           // 기타 - 회색
    )

    fun getSectorColor(groupCode: String): Color {
        return sectorColors[groupCode] ?: Color(0xFF9E9E9E)
    }
}
```

### 10.5 버블 클러스터 Composable (상세)

```kotlin
// ui/components/EtfSectorCluster.kt
@Composable
fun EtfSectorCluster(
    data: EtfClusterData,
    onSectorClick: (SectorBubble) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA))
            .onSizeChanged { canvasSize = it }
    ) {
        // 1. Canvas로 버블 그리기
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data.sectors) {
                    detectTapGestures { tapOffset ->
                        // 탭한 위치에 있는 버블 찾기
                        data.sectors.find { sector ->
                            val bubbleCenter = Offset(
                                sector.posX * size.width,
                                sector.posY * size.height
                            )
                            val bubbleRadius = sector.radius * size.width
                            (tapOffset - bubbleCenter).getDistance() <= bubbleRadius
                        }?.let { clickedSector ->
                            onSectorClick(clickedSector)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 연결선 그리기 (중심 → 각 버블)
            data.sectors.forEach { sector ->
                val bubbleCenter = Offset(sector.posX * width, sector.posY * height)
                val centerPoint = Offset(width * 0.5f, height * 0.5f)

                drawLine(
                    color = sector.color.copy(alpha = 0.3f),
                    start = centerPoint,
                    end = bubbleCenter,
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // 섹터 버블 그리기
            data.sectors.forEach { sector ->
                val bubbleCenter = Offset(sector.posX * width, sector.posY * height)
                val bubbleRadius = sector.radius * width
                val isSelected = data.selectedSector?.id == sector.id

                // 버블 배경
                drawCircle(
                    color = sector.color.copy(alpha = if (isSelected) 0.5f else 0.2f),
                    radius = bubbleRadius,
                    center = bubbleCenter
                )

                // 버블 테두리
                drawCircle(
                    color = sector.color,
                    radius = bubbleRadius,
                    center = bubbleCenter,
                    style = Stroke(width = if (isSelected) 4.dp.toPx() else 2.dp.toPx())
                )
            }

            // 중심 ETF 원
            val centerRadius = width * 0.12f

            // 그라데이션 효과
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ClusterColors.CenterGreen,
                        ClusterColors.CenterGreenDark
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = centerRadius
                ),
                radius = centerRadius,
                center = Offset(width * 0.5f, height * 0.5f)
            )

            // 중심 테두리
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = centerRadius,
                center = Offset(width * 0.5f, height * 0.5f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // 2. 중심 ETF 라벨
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size((canvasSize.width * 0.24f).pxToDp()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.etfName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3. 섹터 라벨들
        data.sectors.forEach { sector ->
            SectorBubbleLabel(
                sector = sector,
                canvasSize = canvasSize,
                isSelected = data.selectedSector?.id == sector.id,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}

@Composable
private fun SectorBubbleLabel(
    sector: SectorBubble,
    canvasSize: IntSize,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // 라벨 위치 계산 (버블 중심)
    val offsetX = with(density) { (sector.posX * canvasSize.width).toDp() }
    val offsetY = with(density) { (sector.posY * canvasSize.height).toDp() }
    val bubbleRadius = with(density) { (sector.radius * canvasSize.width).toDp() }

    Box(
        modifier = modifier
            .offset(
                x = offsetX - bubbleRadius,
                y = offsetY - bubbleRadius
            )
            .size(bubbleRadius * 2),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 섹터명
            Text(
                text = sector.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) sector.color else Color.DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 비중
            Text(
                text = String.format("%.1f%%", sector.weightPct),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) sector.color else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// px → dp 변환 확장 함수
@Composable
private fun Float.pxToDp(): Dp {
    return with(LocalDensity.current) { this@pxToDp.toDp() }
}
```

### 10.6 선택된 섹터 상세 정보 카드

```kotlin
// ui/components/SectorDetailCard.kt
@Composable
fun SectorDetailCard(
    sector: SectorBubble,
    onClose: () -> Unit,
    onViewStocks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = sector.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, sector.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(sector.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sector.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 상세 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoColumn(label = "비중", value = String.format("%.1f%%", sector.weightPct))
                InfoColumn(label = "종목 수", value = "${sector.stockCount}개")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 종목 보기 버튼
            OutlinedButton(
                onClick = onViewStocks,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = sector.color
                ),
                border = BorderStroke(1.dp, sector.color)
            ) {
                Text("이 섹터 종목 보기")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### 10.7 애니메이션 적용

```kotlin
// ui/components/AnimatedEtfSectorCluster.kt
@Composable
fun AnimatedEtfSectorCluster(
    data: EtfClusterData,
    onSectorClick: (SectorBubble) -> Unit,
    modifier: Modifier = Modifier
) {
    // 등장 애니메이션
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = false
        delay(100)
        isVisible = true
    }

    // 각 버블의 애니메이션 상태
    val animatedSectors = data.sectors.mapIndexed { index, sector ->
        val animatedRadius by animateFloatAsState(
            targetValue = if (isVisible) sector.radius else 0f,
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = index * 50,  // 순차적으로 등장
                easing = FastOutSlowInEasing
            ),
            label = "radius_$index"
        )

        val animatedAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50
            ),
            label = "alpha_$index"
        )

        sector.copy(radius = animatedRadius) to animatedAlpha
    }

    // 선택 시 스케일 애니메이션
    val selectedScale by animateFloatAsState(
        targetValue = if (data.selectedSector != null) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectedScale"
    )

    EtfSectorCluster(
        data = data.copy(
            sectors = animatedSectors.map { (sector, _) -> sector }
        ),
        onSectorClick = onSectorClick,
        modifier = modifier.graphicsLayer {
            scaleX = selectedScale
            scaleY = selectedScale
        }
    )
}
```

### 10.8 전체 화면 통합 예시

```kotlin
// ui/screen/EtfClusterScreen.kt
@Composable
fun EtfClusterScreen(
    etfId: Long,
    onNavigateToSectorStocks: (String) -> Unit,
    viewModel: EtfClusterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ETF 섹터 분석") },
                navigationIcon = {
                    IconButton(onClick = { /* 뒤로가기 */ }) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is EtfClusterUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is EtfClusterUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = viewModel::loadClusterData,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is EtfClusterUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // ETF 정보 헤더
                        Text(
                            text = state.data.etfName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "기준일: ${state.data.baseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 클러스터 시각화
                        AnimatedEtfSectorCluster(
                            data = state.data,
                            onSectorClick = viewModel::onSectorClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        // 선택된 섹터 상세
                        AnimatedVisibility(
                            visible = state.data.selectedSector != null,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            state.data.selectedSector?.let { sector ->
                                SectorDetailCard(
                                    sector = sector,
                                    onClose = viewModel::clearSelection,
                                    onViewStocks = {
                                        onNavigateToSectorStocks(sector.id)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 섹터 목록 (텍스트)
                        Text(
                            text = "섹터 구성",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        state.data.sectors
                            .sortedByDescending { it.weightPct }
                            .forEach { sector ->
                                SectorListItem(
                                    sector = sector,
                                    isSelected = state.data.selectedSector?.id == sector.id,
                                    onClick = { viewModel.onSectorClicked(sector) }
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorListItem(
    sector: SectorBubble,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) sector.color.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(sector.color, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = sector.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = String.format("%.1f%%", sector.weightPct),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = sector.color
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${sector.stockCount}종목",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
```

### 10.9 Navigation 설정

```kotlin
// navigation/EtfNavigation.kt
fun NavGraphBuilder.etfNavGraph(
    navController: NavController
) {
    composable(
        route = "etf/{etfId}/cluster",
        arguments = listOf(navArgument("etfId") { type = NavType.LongType })
    ) { backStackEntry ->
        val etfId = backStackEntry.arguments?.getLong("etfId") ?: 0L

        EtfClusterScreen(
            etfId = etfId,
            onNavigateToSectorStocks = { sectorId ->
                navController.navigate("etf/$etfId/sector/$sectorId/stocks")
            }
        )
    }
}

// 화면 이동
fun NavController.navigateToEtfCluster(etfId: Long) {
    navigate("etf/$etfId/cluster")
}
```

---

## 11. ASSET_TYPE 클러스터 (파생상품 ETF)

### 11.1 대상 ETF (12개)

| stock_code | ETF명 | 주요 자산 |
|------------|-------|----------|
| 114800 | KODEX 인버스 | KOSPI200 선물 (숏) |
| 123310 | TIGER 인버스 | KOSPI200 선물 (숏) |
| 132030 | KODEX 골드선물(H) | 금 선물, USD 선물 |
| 319640 | TIGER 골드선물(H) | 금 선물, USD 선물 |
| 261140 | TIGER 우선주 | 우선주 20종 |
| 267770 | TIGER 200선물레버리지 | KOSPI200 선물, ETF |
| 252670 | KODEX 200선물인버스2X | KOSPI200 선물, ETF |
| 252710 | TIGER 200선물인버스2X | KOSPI200 선물, ETF |
| 250780 | TIGER 코스닥150선물인버스 | 코스닥150 선물 |
| 251340 | KODEX 코스닥150선물인버스 | 코스닥150 선물, ETF |
| 280940 | KODEX 골드선물인버스(H) | 금 선물, USD 선물 |
| 360150 | KODEX 코스닥150롱코스피200숏선물 | 코스닥150 선물, KOSPI200 선물 |

### 11.2 자산 유형 (asset_type)

| asset_type | 한글명 | 설명 |
|------------|--------|------|
| FUTURES | 선물 | KOSPI200, 코스닥150, 금, USD 선물 |
| BOND | 채권/RP | 국고채, RP |
| CASH | 현금성 자산 | 예금, MMF |
| ETF | ETF | ETF 내 ETF 보유 |
| PREFERRED_STOCK | 우선주 | 삼성전자우, 현대차2우B 등 |

### 11.3 비중 계산 로직

```sql
-- weight > 0 이면 weight 사용
-- weight = 0 이면 ABS(market_value) 비율로 계산
CASE
    WHEN total_weight > 0 THEN sum_weight
    WHEN total_mv > 0 THEN ROUND((sum_mv / total_mv * 100)::numeric, 2)
    ELSE 0
END as weight_pct
```

### 11.4 클러스터 생성 스크립트

```bash
# 파생상품 ETF만
python -m scripts.generate_sector_cluster --mode derivative

# 전체 (주식형 + 파생상품)
python -m scripts.generate_sector_cluster --mode all
```

---

## 12. 향후 확장

1. **터치 인터랙션**: 버블 클릭 시 해당 섹터 종목 목록 표시 ✅ (10.6, 10.8에 구현)
2. **애니메이션**: 버블 등장/이동 애니메이션 ✅ (10.7에 구현)
3. **줌/패닝**: 클러스터 확대/축소 기능 (TransformableState 활용)
4. **시계열 분석**: 섹터 분포 변화 추적 (base_date 활용)
5. **포트폴리오 섹터 분석**: 사용자 포트폴리오에도 동일 로직 적용
6. **ASSET_TYPE 시각화**: 파생상품 ETF용 자산 유형별 클러스터 UI
