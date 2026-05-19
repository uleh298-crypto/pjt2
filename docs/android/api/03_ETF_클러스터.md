# ETF 클러스터 API

> 화면: `EtfDetailScreen.kt`, `ClusterTab.kt`, `SectorBottomSheet.kt`

---

## 코드값 정의

### 위험등급 (riskLevel)

ETF의 투자 위험도를 1~5 단계로 나타냅니다. 금융투자협회 기준에 따라 분류됩니다.

| riskLevel | 표시 텍스트 | 색상 | 설명 | 투자 성향 |
|-----------|------------|------|------|----------|
| `1` | 안정형 | Blue (#2196F3) | 원금 손실 가능성 매우 낮음 | 예금 대안, 안정 추구 |
| `2` | 안정추구형 | Teal (#009688) | 원금 손실 가능성 낮음 | 채권형, 안정 수익 추구 |
| `3` | 위험중립형 | Yellow (#FFC107) | 원금 손실 가능성 보통 | 혼합형, 균형 투자 |
| `4` | 적극투자형 | Orange (#FF9800) | 원금 손실 가능성 높음 | 주식형, 성장 추구 |
| `5` | 공격투자형 | Red (#F44336) | 원금 손실 가능성 매우 높음 | 레버리지/인버스, 고수익 추구 |

### 변동성 (volatility)

1년 기준 ETF 가격 변동 정도를 나타냅니다.

| volatility | 설명 | 기준 (연간 표준편차) |
|------------|------|---------------------|
| `매우 낮음` | 가격 변동이 거의 없음 | < 5% |
| `낮음` | 가격 변동이 적음 | 5% ~ 10% |
| `보통` | 일반적인 변동 수준 | 10% ~ 20% |
| `높음` | 가격 변동이 큼 | 20% ~ 30% |
| `매우 높음` | 가격 변동이 매우 큼 | > 30% |

### 섹터명 & 아이콘 매핑

ETF 구성종목의 산업 섹터를 분류합니다. 클러스터 버블 차트에서 사용됩니다.

#### 주식 섹터 (assetType: null)

`industry_classification` 테이블의 `group_name` 기준

| 섹터명 | Material Icon |
|--------|---------------|
| 반도체 | `Memory` |
| 금융 | `AccountBalance` |
| 바이오/의약 | `LocalHospital` |
| 에너지/유틸리티 | `Bolt` |
| 소프트웨어 | `Computer` |
| 소비재 | `ShoppingCart` |
| 산업재 | `Factory` |
| 통신/미디어 | `CellTower` |
| 자동차 | `DirectionsCar` |
| 화학/소재 | `Science` |
| 건설 | `Home` |
| 조선 | `DirectionsBoat` |
| 운송 | `LocalShipping` |
| 식품/음료 | `Restaurant` |
| 철강/금속 | `Hardware` |
| 기계 | `Precision` |
| 유통/소매 | `Store` |
| 보험 | `Security` |
| 지주회사 | `Business` |
| 전자/IT | `Devices` |
| 이벤트/테마 | `Category` |

#### 비주식 자산 (assetType 값으로 구분)

레버리지/인버스 ETF 등 주식 외 자산을 보유하는 ETF의 구성종목입니다.

| assetType | 표시명 | Material Icon | 설명 |
|-----------|--------|---------------|------|
| `FUTURES` | 선물 | `ShowChart` | KOSPI200 선물, 금 선물 등 합산 |
| `ETF` | ETF | `Layers` | KODEX 인버스, TIGER 200 등 합산 |
| `BOND` | (채권유형별) | `Description` | 채권 유형별로 섹터 분리 (아래 참고) |
| `CASH` | 현금 | `Payments` | 현금성 자산 합산 |
| `COMMODITY` | 원자재 | `Diamond` | 금현물, 원유 등 |
| `REITS` | 리츠 | `Apartment` | 부동산/인프라 |
| `MIXED` | 혼합자산 | `PieChart` | TDF, TRF 등 |
| `PREFERRED_STOCK` | 우선주 | `Star` | 삼성전자우, 현대차2우B 등 합산 |

#### 채권 유형 (assetType: BOND인 경우)

채권형 ETF는 **채권 유형별로 섹터가 분리**됩니다. `sectors[].name`에 채권 유형명이 들어갑니다.

| 채권 유형 | 설명 | 코드 패턴 |
|-----------|------|-----------|
| 국고채 | 정부 발행 채권 | 035xxx |
| 산금채 | 산업금융채권 | 079xxx |
| 은행채 | 은행 발행 채권 | 008xxx |
| 회사채 | 기업 발행 채권 | Fxxxxx |
| 통안채 | 통화안정증권 | 010xxx |
| 지방채 | 지방정부 채권 | 019xxx |
| 특수채 | 특수목적 채권 | 021xxx, 029xxx |
| 금융채 | 금융기관 채권 | 030xxx, 032xxx, 033xxx |
| 기타채권 | 기타 | - |

### 에러 코드

| 코드 | HTTP 상태 | 메시지 | 설명 | 대응 방법 |
|------|----------|--------|------|----------|
| `ETF001` | 404 | ETF를 찾을 수 없습니다 | 존재하지 않는 ticker | 올바른 티커 확인 |
| `ETF002` | 404 | 시세 정보가 없습니다 | ETF 가격 데이터 없음 | 장 마감 후 조회 또는 새로고침 |

---

## GET /api/v1/etfs/{ticker}/clusters

ETF 클러스터 조회 (섹터 클러스터 + 영향력 종목)

> ETF 기본 정보는 `GET /api/v1/etfs/{ticker}`에서 조회

### Request

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| ticker | String | Y | ETF 티커 (예: "091160") |

### Response

#### 예시 1: 레버리지 ETF (주식 + 선물)

```json
{
  "success": true,
  "message": "Success",
  "code": "OK",
  "data": {
    "englishName": "KOSPI 200 Futures Leverage Fund",
    "sectors": [
      {
        "name": "반도체",
        "percentage": 20.1,
        "assetType": null,
        "stocks": [
          {"ticker": "005930", "name": "삼성전자", "percentage": 12.768},
          {"ticker": "000660", "name": "SK하이닉스", "percentage": 7.361}
        ],
        "aiAnalysis": "반도체 섹터는 AI 수요 증가로 인해 성장세가 지속될 것으로 예상됩니다."
      },
      {
        "name": "선물",
        "percentage": 84.432,
        "assetType": "FUTURES",
        "stocks": [
          {"ticker": "A01630", "name": "KOSPI200 선물 2503", "percentage": 84.432}
        ],
        "aiAnalysis": "KOSPI200 선물에 투자하는 고위험-고수익 전략의 ETF입니다."
      }
    ],
    "influentialStocks": [
      {"ticker": "005930", "name": "삼성전자", "weight": 12.768, "currentPrice": 72300, "changeRate": 1.85}
    ]
  },
  "timestamp": "2025-03-10T14:30:00"
}
```

#### 예시 2: 채권형 ETF (채권 유형별 섹터 분리)

**GET /api/v1/etfs/0139F0/clusters** (TIGER 12월자동연장금융채)

```json
{
  "success": true,
  "message": "Success",
  "code": "OK",
  "data": {
    "englishName": null,
    "sectors": [
      {
        "name": "회사채",
        "percentage": 47.304,
        "assetType": "BOND",
        "stocks": [
          {"ticker": "F14017", "name": "F14017", "percentage": 2.5},
          {"ticker": "F00123", "name": "F00123", "percentage": 1.8}
        ],
        "aiAnalysis": "회사채는 기업이 발행한 채권으로..."
      },
      {
        "name": "기타채권",
        "percentage": 27.885,
        "assetType": "BOND",
        "stocks": [
          {"ticker": "06590B", "name": "06590B", "percentage": 4.64},
          {"ticker": "81101G", "name": "81101G", "percentage": 2.32}
        ],
        "aiAnalysis": null
      },
      {
        "name": "특수채",
        "percentage": 9.336,
        "assetType": "BOND",
        "stocks": [
          {"ticker": "029883", "name": "029883", "percentage": 1.58}
        ],
        "aiAnalysis": null
      },
      {
        "name": "산금채",
        "percentage": 2.328,
        "assetType": "BOND",
        "stocks": [
          {"ticker": "07931A", "name": "07931A", "percentage": 1.57}
        ],
        "aiAnalysis": null
      }
    ],
    "influentialStocks": []
  },
  "timestamp": "2025-03-26T14:30:00"
}
```

> **참고**: `assetType`이 `null`이면 주식 섹터, 값이 있으면 비주식 자산입니다.

> **참고**: `assetType`이 `BOND`인 경우 채권 유형별로 섹터가 분리됩니다. `sectors[].name`에 채권 유형명(회사채, 국고채 등)이 들어갑니다.

### 화면 매핑

#### 클러스터 버블 차트
| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 버블 내 섹터명 |
| sectors[].percentage | 버블 내 비중 % |
| sectors[].name | 아이콘 매핑 (반도체→Memory, 금융→AccountBalance 등) |

버블 크기: `percentage` 값에 비례

#### 영향력 종목 섹션
| API 필드 | 화면 표시 |
|----------|----------|
| influentialStocks[].name | 종목명 (아바타 첫글자) |
| influentialStocks[].weight | 비중 % |
| influentialStocks[].currentPrice | 현재가 |
| influentialStocks[].changeRate | 등락률 (초록/빨강) |

#### 섹터 바텀시트 (SectorBottomSheet)
| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 섹터명 |
| sectors[].percentage | 섹터 비중 |
| sectors[].stocks[].name | 종목명 |
| sectors[].stocks[].percentage | 종목 비중 |
| sectors[].aiAnalysis | AI 분석 텍스트 (있으면 표시) |

#### 비주식 자산 버블

`sectors[]`에서 `assetType`이 null이 아닌 항목은 비주식 자산입니다.

| API 필드 | 화면 표시 |
|----------|----------|
| sectors[].name | 자산 유형명 (선물, ETF, 원자재, 리츠, 혼합자산, 현금, 우선주) 또는 채권유형명 (회사채, 국고채 등) |
| sectors[].percentage | 비중 % (버블 크기) |
| sectors[].assetType | 아이콘 매핑 (FUTURES→ShowChart, ETF→Layers, BOND→Description 등) |
| sectors[].stocks[] | 개별 자산 목록 (바텀시트에서 표시) |

> **참고**: 주식 섹터와 비주식 자산 모두 `stocks[]`에 개별 항목이 포함됩니다. 프론트는 동일한 로직으로 렌더링합니다.

> **참고**: `assetType`이 `BOND`인 경우 `name`에 채권 유형명(회사채, 국고채 등)이 들어갑니다. 아이콘은 `assetType`으로 매핑하세요.

---

## 백엔드 구현 상태

- [x] `EtfController.java` 구현 완료
- [x] `EtfService.java` 구현 완료
- [x] `EtfClusterResponse.java` DTO 구현 완료

### 구현된 조회 로직

```
etf (기본 정보 - englishName)
    ↓
etf_sector_cluster (섹터 클러스터 - 버블 차트)
    ↓
etf_sector_ai_history (섹터 AI 분석)
    ↓
etf_stock_composition → stock → company_info (구성 종목)
    ↓
etf_other_composition (비주식 구성종목 - 선물, 채권, ETF 등)
```

- 섹터별 상위 5개 종목 반환
- 영향력 종목 상위 5개 반환 (주식 구성종목이 있는 ETF만)
- 비주식 구성종목 처리:
  - FUTURES → "선물" (비중 합산)
  - ETF → "ETF" (비중 합산)
  - PREFERRED_STOCK → "우선주" (비중 합산)
  - **BOND → 채권 유형별 섹터 분리** (회사채, 국고채, 산금채, 은행채 등)
  - CASH → "현금" (비중 합산)
  - COMMODITY → "원자재"
  - REITS → "리츠"
  - MIXED → "혼합자산"
- 위험등급: AGGRESSIVE → 5, STABLE → 1 매핑
- 변동성: 1년 변동률 기준 문자열 변환

## 안드로이드 구현 상태

- [x] `EtfApiService.kt` 구현 완료
- [x] `EtfDetailResponse.kt` DTO 구현 완료
- [x] API 연동 완료

> **참고**: 비주식 자산 그룹핑은 백엔드에서 처리되므로 프론트엔드 수정 불필요. `sectors[]` 배열을 그대로 렌더링하면 됨.
