package com.d102.wye.domain.model

/**
 * 인증 토큰 + 유저 정보 Domain Model
 *
 * loginProvider는 서버 문자열("EMAIL", "KAKAO")을 그대로 쓰지 않고
 * domain에서 LoginProvider enum으로 타입 안전하게 관리
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val isNewUser: Boolean,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val email: String?,
    val nickname: String,
    val profileImage: String?,
    val loginProvider: LoginProvider
)

enum class LoginProvider {
    EMAIL, KAKAO, UNKNOWN;

    companion object {
        fun from(value: String): LoginProvider = when (value.uppercase()) {
            "EMAIL" -> EMAIL
            "KAKAO" -> KAKAO
            else    -> UNKNOWN
        }
    }
}
