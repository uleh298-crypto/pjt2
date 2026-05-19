package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SignupResponse(
    @SerializedName("email")
    val email: String
)
