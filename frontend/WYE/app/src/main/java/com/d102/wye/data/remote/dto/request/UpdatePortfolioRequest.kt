package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdatePortfolioRequest(
    @SerializedName("portfolioId") val portfolioId: Long,
    @SerializedName("name") val name: String
)