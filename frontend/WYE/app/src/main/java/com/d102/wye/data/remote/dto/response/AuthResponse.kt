package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("expiresIn")
    val expiresIn: Int,             // 토큰 만료 시간 (초)

    @SerializedName("isNewUser")
    val isNewUser: Boolean,

    @SerializedName("user")
    val user: UserResponse
)

data class UserResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("email")
    val email: String?,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("profileImage")
    val profileImage: String?,      // nullable (미설정 시 null)

    @SerializedName("loginProvider")
    val loginProvider: String       // "EMAIL" | "KAKAO"
)
