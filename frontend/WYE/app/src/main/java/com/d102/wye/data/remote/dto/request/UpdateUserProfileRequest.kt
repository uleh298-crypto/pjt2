package com.d102.wye.data.remote.dto.request

data class UpdateUserProfileRequest(
    val nickname: String? = null,
    val profileImage: String? = null
)
