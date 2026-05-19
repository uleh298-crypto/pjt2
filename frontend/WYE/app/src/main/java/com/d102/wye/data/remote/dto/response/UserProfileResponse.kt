package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("profileImage")
    val profileImage: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String?,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("socialAccounts")
    val socialAccounts: List<SocialAccountResponse>? = null
)

data class SocialAccountResponse(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("isPrimary")
    val isPrimary: Boolean,
    @SerializedName("linkedAt")
    val linkedAt: String
)
