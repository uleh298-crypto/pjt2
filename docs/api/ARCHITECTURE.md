# Android Clean Architecture 프로젝트 세팅 매뉴얼

> **대상**: Jetpack Compose + Hilt + Coroutines/Flow + Retrofit + Room + DataStore 기반 ETF 투자 분석 앱
> **목표**: 새 기능을 추가할 때 이 문서만 보고 파일 위치를 결정할 수 있게 하는 것

---

## 1. 전체 구조 한눈에 보기

```
com.example.etf/
│
├── core/           ← 앱 인프라 (DI, 네트워크 설정, 유틸)
├── domain/         ← 비즈니스 로직의 중심 (순수 Kotlin, Android 의존성 0)
├── data/           ← 외부 세계와의 통신 (Retrofit API, Room DB, DataStore)
└── presentation/   ← 사용자가 보고 만지는 화면 (Compose UI)
```

### 의존성 방향 (절대 규칙)

```
presentation  ──→  domain  ←──  data
     │                ↑           │
     └──→  core  ←────┘──────────┘
```

- **domain은 아무것도 참조하지 않는다** (최상위 계층)
- data는 domain을 참조한다 (인터페이스 구현)
- presentation은 domain을 참조한다 (UseCase / Repository 호출)
- **presentation은 data를 직접 참조하지 않는다** (Hilt DI가 연결해줌)
- core는 모든 계층에서 참조 가능

---

## 2. 각 계층 상세 설명

### 2-1. domain/ — 앱의 심장

```
domain/
├── model/          ← 순수 Kotlin 데이터 클래스
├── repository/     ← 인터페이스만 (구현체 없음)
├── usecase/        ← 비즈니스 로직 단위
├── state/          ← 상태 머신용 Sealed Class, Enum
└── common/         ← Result 래퍼, 에러 타입
```

#### 왜 domain이 최상위인가?

**domain은 "이 앱이 무엇을 하는가"를 정의하는 계층이다.**

서버 API 스펙이 바뀌어도, UI가 Compose에서 XML로 바뀌어도,
domain 코드는 한 줄도 바뀌지 않아야 한다.
그래서 Android import가 하나도 없어야 한다.

#### domain/model/ — 순수 Kotlin 데이터 클래스

```kotlin
// 올바른 domain model — Android import 없음
data class Etf(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,         // 등락률 (%)
    val volume: Long,               // 매매량
    val riskLevel: Int,             // 위험등급 1~5
    val investmentStrategy: String, // 투자전략
    val assetClass: String,         // 기초자산/섹터
    val dividendYield: Double       // 분배율 (배당수익률)
)

data class EtfDetail(
    val etf: Etf,
    val constituents: List<EtfConstituent>,  // 구성 종목 TOP 10
    val returnRate1M: Double,
    val returnRate6M: Double,
    val returnRate1Y: Double,
    val expenseRatio: Double,                // 운용 보수
    val per: Double,
    val pbr: Double,
    val roe: Double
)


// ❌ domain model에 들어가면 안 되는 것
// data class EtfUiItem(
//     val etf: Etf,
//     val isLiked: Boolean,
//     val riskTagColor: Color,        ← Compose 의존
//     @DrawableRes val sectorIcon: Int ← R.drawable 의존
// )
// → 이건 presentation/model/EtfUiItem.kt으로 가야 한다
```

**판별 기준**: `import android.*`, `import androidx.*`, `import com.google.android.*`가 있으면 domain이 아니다.

#### domain/repository/ — 인터페이스만

```kotlin
// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): BaseResult<TokenPair>
    suspend fun signup(email: String, password: String, nickname: String): BaseResult<Unit>
    suspend fun sendVerificationCode(email: String): BaseResult<Unit>
    suspend fun verifyCode(email: String, code: String): BaseResult<Unit>
    suspend fun resetPassword(email: String, newPassword: String): BaseResult<Unit>
    fun getAccessToken(): Flow<String?>
}

// domain/repository/EtfRepository.kt
interface EtfRepository {
    fun getEtfList(): Flow<List<Etf>>                                    // 1분 polling
    suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail>
    suspend fun toggleLike(ticker: String): BaseResult<Boolean>
    fun getLikedEtfList(): Flow<List<Etf>>                               // Room 캐시
    suspend fun searchEtf(query: String, filter: EtfFilterState): BaseResult<List<Etf>>
}
```

**왜 인터페이스가 domain에 있는가?**

Repository 인터페이스는 "이 앱에 어떤 데이터 조작이 필요한가"를 선언한다.
이것은 비즈니스 요구사항이지, 구현 세부사항이 아니다.

- "ETF 목록 조회가 필요하다" → domain이 선언
- "Retrofit으로 GET /etf/list를 1분마다 호출한다" → data가 구현

```
domain/repository/EtfRepository.kt       ← "무엇이 필요한가" (인터페이스)
data/repository/EtfRepositoryImpl.kt     ← "어떻게 구현하는가" (구현체)
core/di/RepositoryModule.kt              ← "둘을 연결해준다" (Hilt @Binds)
```

#### domain/usecase/ — 비즈니스 로직

```kotlin
// domain/usecase/portfolio/CalculateWeightUseCase.kt
// → 비중 합산 검증: 시뮬레이션 설정 + 종목 추가 화면 2곳에서 재사용
class CalculateWeightUseCase {
    operator fun invoke(portfolios: List<Portfolio>): WeightValidationResult {
        val total = portfolios.sumOf { it.weightPercent }
        return when {
            total > 100.0 -> WeightValidationResult.Exceeded(total)
            total == 100.0 -> WeightValidationResult.Valid
            else -> WeightValidationResult.Incomplete(remaining = 100.0 - total)
        }
    }
}

// domain/usecase/portfolio/CalcExpectedDividendUseCase.kt
// → 총 투자금 × 비중 × 각 ETF 분배율 합산
class CalcExpectedDividendUseCase {
    operator fun invoke(totalAmount: Long, portfolios: List<Portfolio>): Long {
        return portfolios.sumOf { portfolio ->
            val invested = totalAmount * (portfolio.weightPercent / 100.0)
            (invested * portfolio.etf.dividendYield).toLong()
        }
    }
}

// domain/usecase/portfolio/CalcFundamentalUseCase.kt
// → PER/PBR/ROE 가중평균 계산
class CalcFundamentalUseCase {
    operator fun invoke(portfolios: List<Portfolio>): FundamentalResult {
        val totalWeight = portfolios.sumOf { it.weightPercent }
        if (totalWeight == 0.0) return FundamentalResult.empty()
        return FundamentalResult(
            per = portfolios.sumOf { it.etf.per * (it.weightPercent / totalWeight) },
            pbr = portfolios.sumOf { it.etf.pbr * (it.weightPercent / totalWeight) },
            roe = portfolios.sumOf { it.etf.roe * (it.weightPercent / totalWeight) }
        )
    }
}

// domain/usecase/etf/FilterEtfListUseCase.kt
// → 탐색 화면 + 시뮬레이션 종목 추가 화면 2곳에서 재사용
class FilterEtfListUseCase {
    operator fun invoke(etfList: List<Etf>, filter: EtfFilterState): List<Etf> {
        return etfList
            .filter { filter.riskLevels.isEmpty() || it.riskLevel in filter.riskLevels }
            .filter { filter.assetClass == null || it.assetClass == filter.assetClass }
            .filter { filter.strategy == null || it.investmentStrategy == filter.strategy }
            .filter {
                filter.query.isBlank() ||
                it.name.contains(filter.query, ignoreCase = true) ||
                it.ticker.contains(filter.query, ignoreCase = true)
            }
    }
}
```

**왜 UseCase를 분리하는가?**

| UseCase 없이 | UseCase 있을 때 |
|---|---|
| SimulationViewModel에서 비중 검증 로직 직접 작성 | `CalculateWeightUseCase` 단독 테스트 가능 |
| 탐색/시뮬레이션 양쪽에 필터 로직 중복 | `FilterEtfListUseCase` 하나로 재사용 |
| PER/PBR/ROE 계산이 ViewModel에 묻힘 | `CalcFundamentalUseCase`만 수정 |

**UseCase 도입 기준** (모든 Repository에 다 만들 필요 없음):
1. 같은 로직을 여러 ViewModel이 사용할 때
2. 비즈니스 규칙이 있는가 (계산, 검증, 상태 전이)
3. 여러 Repository를 조합하는 로직이 있을 때
4. 단위 테스트가 필요한 로직일 때

단순 CRUD (관심 ETF 토글, 전략 삭제 등)는 UseCase 없이 ViewModel → Repository 직접 호출해도 된다.

#### domain/state/ — 상태 정의

```kotlin
// domain/state/InvestmentType.kt
enum class InvestmentType { LUMP_SUM, ACCUMULATE }  // 관망형 / 적립형

// domain/state/EtfFilterState.kt
data class EtfFilterState(
    val query: String = "",
    val riskLevels: Set<Int> = emptySet(),      // 위험등급 필터 (1~5)
    val assetClass: String? = null,             // 기초자산 필터
    val strategy: String? = null               // 투자전략 필터
)

// domain/state/WeightValidationResult.kt
sealed class WeightValidationResult {
    object Valid : WeightValidationResult()
    data class Exceeded(val total: Double) : WeightValidationResult()
    data class Incomplete(val remaining: Double) : WeightValidationResult()
}
```

#### domain/common/ — 공통 타입

```kotlin
// domain/common/BaseResult.kt
sealed class BaseResult<out T> {
    data class Success<T>(val data: T) : BaseResult<T>()
    data class Error(val error: ApiError) : BaseResult<Nothing>()
}

// domain/common/ApiError.kt
data class ApiError(
    val code: Int,
    val message: String
)
```

`BaseResult`와 `ApiError`는 모든 계층에서 사용하지만,
UI 렌더링 상태가 아니라 **데이터 처리 결과**이므로 domain에 위치한다.

반면 `UiState`(Idle/Loading/Success/Error)는 **화면 렌더링 상태**이므로 presentation에 위치한다.

| 타입 | 위치 | 이유 |
|------|------|------|
| `BaseResult<T>` | domain/common | 데이터 성공/실패 (모든 계층에서 사용) |
| `ApiError` | domain/common | 에러 정보 (비즈니스 의미) |
| `UiState<T>` | presentation/model | 화면 렌더링 상태 (UI에서만 사용) |

---

### 2-2. data/ — 외부 세계와의 통신

```
data/
├── remote/
│   ├── api/            ← Retrofit 서비스 인터페이스
│   └── dto/            ← 네트워크 Request/Response 모델
│       ├── request/
│       └── response/
├── local/
│   ├── dao/            ← Room DAO
│   ├── database/       ← AppDatabase
│   ├── entity/         ← Room Entity
│   └── datastore/      ← DataStore 접근 클래스
├── mapper/             ← DTO / Entity ↔ Domain Model 변환
└── repository/         ← domain/repository 인터페이스의 구현체
```


#### data/local/ — DataStore vs Room 사용 기준

| 저장소 | 사용 용도 |
|--------|---------|
| **DataStore** | JWT 토큰, 알림 설정 등 Key-Value 단순 데이터 |
| **Room** | 관심 ETF 로컬 캐시, 저장 전략 오프라인 대응 등 구조화 데이터 |

Room Entity는 **반드시 `data/local/entity/`에만 위치**시킨다. domain/model/에 `@Entity` 어노테이션이 붙는 순간 계층 위반이다.

```
data/local/entity/LikedEtfEntity.kt   ← @Entity (Room 전용)
data/mapper/EtfMapper.kt              ← entityToDomain() 변환
domain/model/Etf.kt                   ← 순수 Kotlin (Android import 없음)
```

#### data/mapper/ — 언제 만드는가
**규칙**: DTO를 domain model로 직접 써도 동작에 문제가 없으면 mapper를 미리 만들 필요 없다. 과도한 추상화는 오히려 해가 된다.

**즉시 필요한 mapper:**
```kotlin
// data/mapper/EtfMapper.kt
fun EtfResponse.toDomain() = Etf(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    volume = volume,
    riskLevel = riskLevel,
    investmentStrategy = investmentStrategy,
    assetClass = assetClass,
    dividendYield = dividendYield ?: 0.0   // ← null 처리 등 변환 로직
)

fun LikedEtfEntity.toDomain() = Etf(
    ticker = ticker,
    name = name,
    // ...
)
```
→ Room Entity와 Retrofit DTO 둘 다 domain model로 변환해야 하므로 반드시 필요

**나중에 만들어도 되는 mapper:**
```kotlin
// data/mapper/AuthMapper.kt
fun LoginResponse.toTokenPair() = TokenPair(accessToken, refreshToken)
```
→ 필드 구조가 동일하면 나중에 달라지는 시점에 추가


#### data/repository/ — 구현체만

```kotlin
// data/repository/EtfRepositoryImpl.kt
@Singleton
class EtfRepositoryImpl @Inject constructor(
    private val etfApiService: EtfApiService,
    private val likedEtfDao: LikedEtfDao,
    private val etfMapper: EtfMapper
) : EtfRepository {

    // 1분마다 polling → Flow로 제공
    override fun getEtfList(): Flow<List<Etf>> = flow {
        while (true) {
            val result = safeApiCall { etfApiService.getEtfList() }
            if (result is BaseResult.Success) {
                emit(result.data.map { etfMapper.toDomain(it) })
            }
            delay(60_000L)
        }
    }

    // Room에서 관심 ETF 즉시 반영
    override fun getLikedEtfList(): Flow<List<Etf>> =
        likedEtfDao.getLikedEtfs().map { entities ->
            entities.map { etfMapper.entityToDomain(it) }
        }

    override suspend fun toggleLike(ticker: String): BaseResult<Boolean> =
        safeApiCall { etfApiService.toggleLike(ticker) }
}

// data/repository/AuthRepositoryImpl.kt
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authTokenDataStore: AuthTokenDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): BaseResult<TokenPair> {
        return safeApiCall {
            authApiService.login(LoginRequest(email, password))
        }.also { result ->
            if (result is BaseResult.Success) {
                authTokenDataStore.saveTokens(result.data)  // ← DataStore에 토큰 저장
            }
        }
    }

    override fun getAccessToken(): Flow<String?> =
        authTokenDataStore.accessToken
}
```

---

### 2-3. presentation/ — 화면

```
presentation/
├── model/              ← UI 전용 모델 (UiState, EtfUiItem with Color/Drawable)
├── designsystem/       ← 앱 전체 재사용 컴포넌트 (EtfCard, PercentSlider, TagChip 등)
├── theme/              ← Color, Typography, Theme
├── navigation/         ← 라우트 정의, NavHost, BottomNav
│
├── main/               ← 메인 화면 (TOP10 그래프 + 포트폴리오 수익률 카드)
│
├── auth/
│   ├── login/          ← 로그인
│   ├── signup/         ← 회원가입 (이메일 인증 포함)
│   └── password/       ← 비밀번호 찾기/재설정
│
├── explore/            ← 탐색: ETF 전체 리스트 + 필터
│
├── etfdetail/          ← ETF 상세
│   ├── EtfDetailScreen.kt
│   ├── EtfDetailViewModel.kt
│   └── component/      ← ETF 상세 전용 컴포넌트
│       ├── EtfMapView.kt           ← 네트워크 그래프
│       └── ConstituentTimeline.kt  ← 구성 변동 타임라인
│
├── simulation/
│   ├── entry/          ← 진입화면 + 꾸러미 선택
│   ├── setup/          ← 투자유형/금액/비중 슬라이더 설정
│   ├── addstock/       ← ETF 추가 (탐색 화면 재사용)
│   └── result/         ← 시뮬레이션 결과 (백테스트 그래프, 섹터 분석, AI 피드백)
│
├── strategy/           ← 나의 전략
│   ├── list/           ← 저장된 전략 목록
│   ├── detail/         ← 전략 상세 + 수립 이후 움직임
│   └── compare/        ← 전략 비교
│
├── news/
│   └── detail/         ← 뉴스 상세 (제목 / AI 요약 / 본문 / 관련 ETF)
│
├── mypage/             ← 관심 ETF / 보유 ETF / 계정 설정
│
└── notification/       ← 알림 목록
```

#### Feature-first로 작성

✅ Feature-first (관련 파일이 한 곳에)
presentation/
├── simulation/
│   └── result/
│       ├── SimulationResultScreen.kt
│       └── SimulationResultViewModel.kt
├── explore/
│   ├── ExploreScreen.kt
│   └── ExploreViewModel.kt
└── designsystem/
    ├── EtfCard.kt              ← 탐색 + 전략 목록 2곳에서 재사용
    └── PercentSlider.kt        ← 시뮬레이션에서 재사용
```

#### presentation/model/ — UI 전용 모델

```kotlin
// presentation/model/UiState.kt
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// presentation/model/EtfUiItem.kt — Android 의존 OK (presentation이니까)
data class EtfUiItem(
    val etf: Etf,                           // domain 타입 참조
    val isLiked: Boolean,
    val riskTagColor: Color,                // ← Compose Color
    @DrawableRes val sectorIcon: Int        // ← R.drawable
)
```

#### presentation/designsystem/ — 앱 전체에서 재사용되는 기본 UI 블록

기존에는 `component`라고 칭했지만 그렇게 되면 모든 Composable이 다 들어가게 된다.
`designsystem`이라고 부르면 **앱 전체에서 재사용되는 기본 UI 블록**만 들어간다.

| 폴더 | 들어가는 것 | 안 들어가는 것 |
|------|-----------|-------------|
| `designsystem/` | EtfCard, PercentSlider, TagChip, FilterBottomSheet | EtfMapView, ConstituentTimeline |
| `etfdetail/component/` | EtfMapView, ConstituentTimeline | EtfCard |
| `simulation/result/` | BacktestChart, SectorPieChart (결과 전용) | PercentSlider |

기준: **2개 이상의 Feature에서 사용되면** designsystem, **하나의 Feature에서만 사용되면** 해당 Feature 폴더.

---

### 2-4. core/ — 앱 인프라

```
core/
├── app/            ← Application 클래스, 앱 전역 상수
├── network/        ← OkHttp Interceptor, 토큰 갱신 로직
├── di/             ← Hilt Module (Network, Database, DataStore, Repository)
├── service/        ← FCM Service (알림 수신)
└── util/           ← 순수 유틸리티 (NumberFormatter, DateFormatter 등)
```

**core에 들어가는 기준:**
1. 특정 Feature에 속하지 않는다
2. 여러 계층에서 참조해야 한다
3. Android 인프라 설정이다

---

## 3. 데이터 흐름 예시

### 로그인 흐름

```
[사용자가 로그인 버튼 탭]
    │
    ▼
presentation/auth/login/LoginViewModel.kt
    │  authRepository.login(email, password)
    ▼
domain/repository/AuthRepository.kt          ← 인터페이스 (domain 계층)
    │  (Hilt가 AuthRepositoryImpl을 주입)
    ▼
data/repository/AuthRepositoryImpl.kt        ← 구현체 (data 계층)
    │  authApiService.login(LoginRequest(...))
    ▼
data/remote/api/AuthApiService.kt
    │  POST /auth/login
    ▼
[서버 응답]
    │
    ▼
data/repository/AuthRepositoryImpl.kt
    │  BaseResult.Success(tokenPair)
    │  authTokenDataStore.saveTokens(tokenPair)  ← DataStore에 토큰 저장
    ▼
presentation/auth/login/LoginViewModel.kt
    │  _uiState.value = UiState.Success
    ▼
presentation/auth/login/LoginScreen.kt
    │  when (uiState) { Success → 메인으로 이동 }
    ▼
[화면 전환]
```

### 시뮬레이션 비중 조절 흐름

```
[슬라이더 조작]
    │
    ▼
presentation/simulation/setup/SimulationSetupViewModel.kt
    │  calculateWeightUseCase(portfolios)
    │  calcExpectedDividendUseCase(totalAmount, portfolios)
    │  calcFundamentalUseCase(portfolios)
    ▼
domain/usecase/portfolio/CalculateWeightUseCase.kt
    │  순수 계산 — 100% 초과 검증
    │  WeightValidationResult 반환
    ▼
domain/usecase/portfolio/CalcExpectedDividendUseCase.kt
    │  총 투자금 × 비중 × 분배율 합산
    │  Long 반환
    ▼
domain/usecase/portfolio/CalcFundamentalUseCase.kt
    │  PER/PBR/ROE 가중평균 계산
    │  FundamentalResult 반환
    ▼
SimulationSetupViewModel.kt
    │  _uiState.update { it.copy(
    │      weightValidation = ...,
    │      expectedDividend = ...,
    │      fundamental = ...
    │  )}
    ▼
presentation/simulation/setup/SimulationSetupScreen.kt
    │  슬라이더 색상 변경 (초과 시 빨간색)
    │  예상 배당금 / PER 등 실시간 렌더링
    │  비중 합계 100% 도달 시 다음 버튼 활성화
```


## 4. 판단 기준 요약 (치트시트)

### 이 파일은 어디에 넣어야 하는가?

```
Q: Android import가 있는가?
├── No  → domain/ 후보
│   Q: 데이터 클래스인가? (Etf, Portfolio, NewsArticle 등)
│   ├── Yes → domain/model/
│   Q: 인터페이스인가? (EtfRepository, PortfolioRepository 등)
│   ├── Yes → domain/repository/
│   Q: 비즈니스 로직인가? (비중 계산, PER 가중평균, ETF 필터링)
│   ├── Yes → domain/usecase/
│   Q: 상태 정의인가? (InvestmentType, EtfFilterState, WeightValidationResult)
│   └── Yes → domain/state/
│
├── Yes → data/ 또는 presentation/ 또는 core/
│   Q: 서버 API / Room / DataStore와 통신하는가?
│   ├── Yes → data/
│   │   Q: Retrofit 인터페이스? → data/remote/api/
│   │   Q: 네트워크 DTO? → data/remote/dto/
│   │   Q: Room Entity? → data/local/entity/
│   │   Q: Room DAO? → data/local/dao/
│   │   Q: DataStore 접근? → data/local/datastore/
│   │   Q: Repository 구현체? → data/repository/
│   │   Q: DTO / Entity ↔ Model 변환? → data/mapper/
│   │
│   Q: 사용자에게 보여지는가? (Screen, ViewModel, Composable)
│   ├── Yes → presentation/
│   │   Q: 2개 이상 Feature에서 재사용? → presentation/designsystem/
│   │   Q: 하나의 Feature 전용? → presentation/[feature]/
│   │   Q: UI 전용 모델? → presentation/model/
│   │
│   Q: 특정 Feature에 속하지 않는 인프라인가?
│   └── Yes → core/
│       Q: DI 모듈? → core/di/
│       Q: 네트워크 인터셉터? → core/network/
│       Q: FCM Service? → core/service/
│       Q: 숫자/날짜 포맷팅 등 범용 유틸? → core/util/
```

### UseCase를 만들어야 하는가?

```
Q: Repository 하나만 호출하고 결과를 그대로 전달하는가?
├── Yes → UseCase 불필요 (예: getEtfDetail, toggleLike, deleteStrategy)

Q: 여러 Repository를 조합하는가?
├── Yes → UseCase 필요

Q: 비즈니스 규칙이 있는가? (계산, 검증, 상태 전이)
├── Yes → UseCase 필요 (예: CalculateWeightUseCase, CalcFundamentalUseCase)

Q: 같은 로직을 2개 이상의 ViewModel이 사용하는가?
├── Yes → UseCase 필요 (예: FilterEtfListUseCase — 탐색 + 시뮬레이션 종목 추가)

Q: 단위 테스트가 필요한 로직인가?
└── Yes → UseCase 필요
```

### Mapper를 만들어야 하는가?

```
Q: DTO / Entity와 Domain Model의 필드가 1:1로 같은가?
├── Yes → Mapper 불필요 (나중에 달라지면 그때 추가)

Q: 서버 응답을 변환해야 하는가? (null 처리, 필드명 변경, 타입 변환)
├── Yes → Mapper 필요 (예: EtfResponse.toDomain())

Q: Room Entity → Domain Model 변환이 필요한가?
└── Yes → Mapper 필요 (예: LikedEtfEntity.toDomain())
```

---

## 5. 흔한 실수와 방지법

### ❌ 실수 1: ViewModel에 비즈니스 로직 직접 작성

```kotlin
// ❌ SimulationSetupViewModel.kt에서
val total = portfolios.sumOf { it.weightPercent }
if (total > 100.0) _isWeightExceeded.value = true   // ← 비즈니스 로직
val dividend = totalAmount * portfolios.sumOf { it.etf.dividendYield * it.weightPercent / 100 }
// → 탐색 화면에서도 이 로직이 필요해지는 순간 중복 발생
```

```kotlin
// ✅ domain/usecase/portfolio/로 분리
class SimulationSetupViewModel @Inject constructor(
    private val calculateWeightUseCase: CalculateWeightUseCase,
    private val calcExpectedDividendUseCase: CalcExpectedDividendUseCase
) : ViewModel() {

    fun onWeightChanged(portfolios: List<Portfolio>) {
        _uiState.update { it.copy(
            weightValidation = calculateWeightUseCase(portfolios),
            expectedDividend = calcExpectedDividendUseCase(totalAmount, portfolios)
        )}
    }
}
```

### ❌ 실수 2: domain model에 Room Entity 어노테이션 붙이기

```kotlin
// ❌ domain/model/Etf.kt
@Entity(tableName = "liked_etf")    // ← Room import → domain 계층 위반
data class Etf(
    @PrimaryKey val ticker: String,
    val name: String
)
```

```kotlin
// ✅ 분리
// data/local/entity/LikedEtfEntity.kt  ← @Entity (Room 전용)
// data/mapper/EtfMapper.kt             ← entityToDomain()
// domain/model/Etf.kt                  ← 순수 Kotlin, Android import 없음
```

### ❌ 실수 3: ViewModel에서 data 계층 직접 참조

```kotlin
// ❌ ViewModel에서 data 패키지 import
import com.example.etf.data.remote.api.EtfApiService

class ExploreViewModel @Inject constructor(
    private val etfApiService: EtfApiService  // ← data 직접 참조
)
```

```kotlin
// ✅ domain 인터페이스만 참조
import com.example.etf.domain.repository.EtfRepository

class ExploreViewModel @Inject constructor(
    private val etfRepository: EtfRepository,            // ← domain 인터페이스
    private val filterEtfListUseCase: FilterEtfListUseCase
)
```

### ❌ 실수 4: StateFlow에 MutableList 넣기

```kotlin
// ❌ 참조 동일성으로 인해 Flow가 변경 감지 못 함
private val _portfolios = MutableStateFlow<MutableList<Portfolio>>(mutableListOf())
_portfolios.value.add(newPortfolio)  // 같은 참조 → emit 안 됨 → UI 갱신 안 됨

// ✅ 불변 리스트 + update 사용
private val _portfolios = MutableStateFlow<List<Portfolio>>(emptyList())
_portfolios.update { it + newPortfolio }  // 새 리스트 생성 → emit 됨
```

### ❌ 실수 5: 필터 로직을 탐색 / 시뮬레이션 양쪽에 중복 작성

```kotlin
// ❌ ExploreViewModel
val filtered = allEtfs.filter { it.riskLevel in selectedRisks && ... }

// ❌ SimulationAddStockViewModel — 같은 코드 또 작성
val filtered = allEtfs.filter { it.riskLevel in selectedRisks && ... }
```

```kotlin
// ✅ FilterEtfListUseCase 하나로 양쪽에서 주입받아 사용
class ExploreViewModel @Inject constructor(
    private val filterEtfListUseCase: FilterEtfListUseCase
) : ViewModel()

class SimulationAddStockViewModel @Inject constructor(
    private val filterEtfListUseCase: FilterEtfListUseCase
) : ViewModel()
```

