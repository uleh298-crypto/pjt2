# 카카오 로그인 연동 가이드 (Android)

## 개요
백엔드 API는 **모바일 SDK 방식**만 지원합니다.
Android에서 카카오 SDK로 로그인 후 받은 `access_token`을 백엔드로 전송하면 JWT 토큰을 발급받습니다.

---

## 1. 카카오 SDK 설정

### 1.1 Gradle 의존성 추가

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("com.kakao.sdk:v2-user:2.19.0")
}
```

### 1.2 Kakao SDK 초기화

```kotlin
// Application.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, "{NATIVE_APP_KEY}")
    }
}
```

> `NATIVE_APP_KEY`는 [카카오 개발자 콘솔](https://developers.kakao.com) > 앱 설정 > 앱 키에서 확인

### 1.3 AndroidManifest.xml 설정

```xml
<manifest>
    <application>
        <!-- 카카오 로그인 커스텀 스킴 -->
        <activity
            android:name="com.kakao.sdk.auth.AuthCodeHandlerActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="oauth"
                    android:scheme="kakao{NATIVE_APP_KEY}" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 2. 카카오 로그인 구현

### 2.1 로그인 및 백엔드 연동

```kotlin
class LoginViewModel : ViewModel() {

    private val authApi: AuthApi = RetrofitClient.authApi

    fun loginWithKakao(context: Context) {
        // 카카오톡 설치 여부에 따라 로그인 방식 선택
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            loginWithKakaoTalk(context)
        } else {
            loginWithKakaoAccount(context)
        }
    }

    private fun loginWithKakaoTalk(context: Context) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            handleKakaoLoginResult(token, error)
        }
    }

    private fun loginWithKakaoAccount(context: Context) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            handleKakaoLoginResult(token, error)
        }
    }

    private fun handleKakaoLoginResult(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e("Kakao", "로그인 실패: ${error.message}")
            return
        }

        token?.let {
            // 백엔드 API 호출
            sendTokenToBackend(it.accessToken)
        }
    }

    private fun sendTokenToBackend(kakaoAccessToken: String) {
        viewModelScope.launch {
            try {
                val request = KakaoLoginRequest(accessToken = kakaoAccessToken)
                val response = authApi.kakaoLogin(request)

                if (response.success) {
                    val data = response.data!!

                    // JWT 토큰 저장
                    TokenManager.saveTokens(
                        accessToken = data.accessToken,
                        refreshToken = data.refreshToken
                    )

                    // 신규 회원 여부 확인
                    if (data.isNewUser) {
                        // 프로필 설정 화면으로 이동
                        navigateToProfileSetup(data.user)
                    } else {
                        // 메인 화면으로 이동
                        navigateToMain()
                    }
                }
            } catch (e: Exception) {
                Log.e("Login", "백엔드 로그인 실패: ${e.message}")
            }
        }
    }
}
```

---

## 3. API 모델 정의

### 3.1 Request/Response DTO

```kotlin
// KakaoLoginRequest.kt
data class KakaoLoginRequest(
    val accessToken: String
)

// AuthResponse.kt
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,        // 초 단위 (3600 = 1시간)
    val isNewUser: Boolean,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val email: String?,
    val nickname: String,
    val profileImage: String?,
    val loginProvider: String   // "KAKAO"
)
```

### 3.2 Retrofit API Interface

```kotlin
interface AuthApi {

    @POST("/api/v1/auth/oauth/kakao")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest
    ): ApiResponse<AuthResponse>

    @POST("/api/v1/auth/token/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<AuthResponse>

    @POST("/api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>
}

data class RefreshTokenRequest(
    val refreshToken: String
)
```

---

## 4. 토큰 관리

### 4.1 TokenManager (DataStore 사용)

```kotlin
object TokenManager {
    private lateinit var dataStore: DataStore<Preferences>

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.first()[ACCESS_TOKEN]
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }
}
```

### 4.2 Retrofit Interceptor (토큰 자동 첨부)

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { TokenManager.getAccessToken() }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
```

---

## 5. API 엔드포인트 정보

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/oauth/kakao` | 카카오 로그인 |
| POST | `/api/v1/auth/token/refresh` | 토큰 갱신 |
| POST | `/api/v1/auth/logout` | 로그아웃 |

### Base URL
- 개발: `http://10.0.2.2:8080` (에뮬레이터)
- 개발: `http://{PC_IP}:8080` (실기기)
- 운영: `https://api.example.com` (예정)

---

## 6. 에러 처리

### 6.1 에러 응답 형식

```json
{
  "success": false,
  "message": "소셜 로그인에 실패했습니다.",
  "timestamp": "2026-03-04T21:00:53"
}
```

### 6.2 주요 에러 케이스

| 상황 | 에러 메시지 | 처리 방법 |
|------|------------|----------|
| 잘못된 카카오 토큰 | 소셜 로그인에 실패했습니다 | 카카오 재로그인 |
| Access Token 만료 | 토큰이 만료되었습니다 | Refresh Token으로 갱신 |
| Refresh Token 만료 | 유효하지 않은 토큰입니다 | 카카오 재로그인 |

---

## 7. 플로우 다이어그램

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   사용자     │     │  Android    │     │   Kakao     │     │   Backend   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │
       │  로그인 버튼 클릭   │                   │                   │
       │──────────────────>│                   │                   │
       │                   │                   │                   │
       │                   │  카카오 로그인 요청  │                   │
       │                   │──────────────────>│                   │
       │                   │                   │                   │
       │                   │  access_token 반환 │                   │
       │                   │<──────────────────│                   │
       │                   │                   │                   │
       │                   │  POST /oauth/kakao (accessToken)      │
       │                   │──────────────────────────────────────>│
       │                   │                   │                   │
       │                   │                   │   사용자 정보 조회   │
       │                   │                   │<──────────────────│
       │                   │                   │                   │
       │                   │                   │   사용자 정보 반환   │
       │                   │                   │──────────────────>│
       │                   │                   │                   │
       │                   │  JWT 토큰 + 사용자 정보 반환             │
       │                   │<──────────────────────────────────────│
       │                   │                   │                   │
       │  로그인 완료/화면전환 │                   │                   │
       │<──────────────────│                   │                   │
       │                   │                   │                   │
```

---

## 8. 체크리스트

- [ ] 카카오 개발자 콘솔에서 앱 등록
- [ ] Android 플랫폼 등록 (패키지명, 키 해시)
- [ ] 동의항목 설정 (닉네임, 프로필 사진, 이메일)
- [ ] Kakao SDK 의존성 추가
- [ ] SDK 초기화 코드 작성
- [ ] 로그인 UI 구현
- [ ] 백엔드 API 연동
- [ ] 토큰 저장/관리 구현
- [ ] 에러 처리 구현

---

## 문의
백엔드 관련 문의: 윤상훈
