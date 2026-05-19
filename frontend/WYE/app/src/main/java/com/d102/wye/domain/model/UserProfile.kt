package com.d102.wye.domain.model

data class UserProfile(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val role: String,
    val isActive: Boolean,
    val lastLoginAt: String?,
    val createdAt: String,
    val socialAccounts: List<SocialAccount>
)

data class SocialAccount(
    val provider: String,
    val email: String?,
    val isPrimary: Boolean,
    val linkedAt: String
)
