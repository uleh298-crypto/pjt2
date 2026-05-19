package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class AiReviewRequest(
    @SerializedName("portfolio") val portfolio: AiPortfolioData
)

data class AiPortfolioData(
    @SerializedName("totalAmount") val totalAmount: Long,
    @SerializedName("investmentType") val investmentType: String,  // "DCA" or "LUMP_SUM"
    @SerializedName("etfs") val etfs: List<AiEtfInfo>
)

data class AiEtfInfo(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("weight") val weight: Int
)