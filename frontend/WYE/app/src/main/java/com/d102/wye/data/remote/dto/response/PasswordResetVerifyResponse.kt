package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PasswordResetVerifyResponse(
    @SerializedName("valid")
    val valid: Boolean
)
