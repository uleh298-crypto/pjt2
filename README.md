# What's Your ETF

> ETF를 찾고, 분석하고, 나만의 전략을 세우는 투자 여정

What's Your ETF는 국내 ETF를 시각적으로 탐색하고, 여러 ETF를 조합해 포트폴리오를 시뮬레이션한 뒤, 저장한 전략을 뉴스와 수익률 흐름으로 추적하는 모바일 투자 분석 서비스입니다.

국내 ETF 시장은 빠르게 커지고 있지만, 초보 투자자에게는 종목 수가 많고 구성 종목, 섹터, 수익률, 리스크 정보를 한 번에 이해하기 어렵습니다. WYE는 단순히 수익률 순위를 보여주는 데서 끝나지 않고, ETF 간 관계와 포트폴리오 결과를 직접 비교하며 스스로 투자 판단을 연습할 수 있도록 돕는 것을 목표로 합니다.

> 본 프로젝트는 투자 정보 탐색과 포트폴리오 분석을 돕는 서비스입니다. 특정 금융상품의 매수, 매도, 보유를 권유하지 않습니다.

## 핵심 흐름

1. **Discover**: 국내 ETF 목록을 검색하고, 필터와 클러스터 맵으로 ETF 간 관계를 탐색합니다.
2. **Simulate**: 관심 ETF를 조합하고 비중을 조절해 포트폴리오를 구성합니다.
3. **Analyze**: 과거 데이터 기반 백테스트, 섹터 분포, 펀더멘털 지표, AI 피드백으로 전략을 검토합니다.
4. **Track**: 저장한 전략의 이후 움직임, 관련 뉴스, 알림을 통해 투자 아이디어를 추적합니다.

## 주요 기능

### 회원 및 인증

- 자체 이메일 회원가입, 이메일 인증, 로그인
- 카카오 소셜 로그인
- 비밀번호 찾기 및 재설정
- FCM 토큰 등록을 통한 사용자별 푸시 알림 기반 마련

### 홈

- 일일 매매량 기준 ETF TOP 10 그래프
- 사용자의 포트폴리오 수익률 카드
- 실시간 ETF 시장 뉴스

### ETF 탐색

- 국내 ETF 전체 목록 조회
- 위험분류, 투자전략, 기초자산, 섹터, 이름, 티커 기반 검색 및 필터링
- ETF 간 상관관계와 구성 종목 유사도를 활용한 2D 맵 렌더링
- 관심 ETF 토글, ETF 태그, 섹터별 상세 정보 제공

### ETF 상세

- 단일 ETF 기본 정보, 현재가, 수익률, 운용 보수 조회
- 구성 종목 TOP 10 및 ETF 수익률 차트
- ETF에 영향력이 큰 종목 렌더링
- ETF 구성 변경 타임라인
- ETF에 포함된 종목의 회사 상세 페이지 연결

### 투자 시뮬레이션

- ETF 선택이 막막한 사용자를 위한 사전 구성 ETF 꾸러미
- 투자 유형 선택, 투자 금액 입력
- ETF별 비중 슬라이더 조절과 총합 100% 검증
- 종목 추가 화면과 검색 기능
- 실제 마이데이터 포트폴리오와 사용자가 만든 포트폴리오 비교
- PER, PBR, ROE 등 펀더멘털 지표의 가중 평균 계산
- 예상 배당금, 섹터 분포, 과거 백테스트 그래프, 수익성 지표 제공
- 포트폴리오 AI 피드백 및 시뮬레이션 스냅샷 저장

### 나의 전략

- 저장된 전략 목록 및 상세 조회
- 전략 수립 이후 ETF 움직임 타임라인 조회
- 저장된 포트폴리오끼리 누적 수익률, 배당 수익률, 변동성 비교

### 뉴스

- ETF 시장 뉴스 목록과 상세 조회
- 제목, AI 요약, 본문, 관련 ETF 목록 제공
- ETF 구성 종목 기반 뉴스 매핑
- 경제 뉴스 크롤링 데이터 기반 ETF 클러스터 하이라이트

### 마이페이지 및 알림

- 관심 ETF와 보유 ETF 조회
- 비밀번호 변경, 로그아웃, 계정 설정
- 이용약관, FAQ, 지원센터
- ETF 상장, 상장폐지, 포트폴리오 리밸런싱, 수익률 변동, 관련 뉴스 알림

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Android | Kotlin, Jetpack Compose, Hilt, Coroutines/Flow, Retrofit, Room, DataStore |
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Data/AI Service | Python, FastAPI |
| Database | PostgreSQL 16 |
| Cache/Queue | Redis 7, RabbitMQ |
| Infra | Docker Compose |
| External Data | 한국투자증권 API, pykrx, 네이버 증권 뉴스 |
| AI | LLM API 연동 기반 뉴스 요약 및 포트폴리오 피드백 |

## 시스템 구조

```text
Android App
    |
    v
Spring Boot User Service
    |-- Auth / User / ETF / Portfolio / News / Alert API
    |-- PostgreSQL
    |-- Redis
    |-- RabbitMQ
    |
    v
FastAPI Data Service
    |-- ETF 데이터 수집 및 캐시 갱신
    |-- 뉴스 수집 및 ETF 매핑
    |-- AI 분석 보조 작업
```

## 프로젝트 구조

```text
.
├── backend/
│   ├── user-service/      # Spring Boot API 서버
│   └── data-service/      # FastAPI 데이터/AI 서비스
├── frontend/
│   └── WYE/               # Android 앱
├── db/                    # 로컬 개발용 DB 초기화 스키마
├── docs/                  # 기획, API, ERD, 아키텍처 문서
├── docker-compose-local.yml
└── .env.example
```

## 로컬 실행

### 1. 환경 변수 준비

```powershell
Copy-Item .env.example .env
```

`.env`의 `change-me-*` 값과 외부 API 키는 본인 로컬 환경에 맞게 설정합니다. 실제 키와 운영 비밀번호는 Git에 커밋하지 않습니다.

### 2. 로컬 인프라 실행

```powershell
docker compose -f docker-compose-local.yml up -d
```

실행되는 주요 서비스:

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- RabbitMQ: `localhost:5672`, management UI `localhost:15672`
- pgAdmin: `localhost:5050`

### 3. Spring Boot API 서버 실행

```powershell
cd backend/user-service
./gradlew bootRun
```

### 4. FastAPI 데이터 서비스 실행

```powershell
cd backend/data-service
python -m venv .venv
./.venv/Scripts/Activate.ps1
pip install -r requirements.txt
fastapi dev app/main.py
```

### 5. Android 앱 실행

`frontend/WYE`를 Android Studio에서 열고 `local.properties`를 로컬 SDK 경로에 맞게 설정한 뒤, 에뮬레이터 또는 실제 기기에서 실행합니다.

## 문서

- Android 구조 가이드: `docs/android/프로젝트_구조_가이드.md`
- API 문서: `docs/android/api/`
- 기획안: `docs/planning/WhatsYourETF_기획안.md`
- ERD: `docs/sql/ERD.sql`
- 공개 전 정리한 제3자 고지: `THIRD_PARTY_NOTICES.md`

## 공개 저장소 주의사항

이 저장소는 public 공개를 위해 실제 운영 데이터, DB 덤프, 크롤링된 뉴스 본문 원본, 로컬 서비스 계정 파일, 원격 서버 유지보수 스크립트를 제외한 형태입니다.

새로운 설정 파일을 추가할 때는 다음 원칙을 지켜야 합니다.

- 실제 `.env`, API 키, 서비스 계정 JSON, keystore, 인증서 파일은 커밋하지 않습니다.
- 예시는 `.env.example` 또는 `*.example` 파일로만 제공합니다.
- 외부 뉴스 본문이나 실제 사용자 데이터가 포함된 덤프 파일은 저장소에 넣지 않습니다.
- public push 전에는 secret scanner를 다시 실행합니다.
