package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PortfolioDetail(
    @SerializedName("portfolioId")   val portfolioId: Long,
    @SerializedName("portfolioName") val portfolioName: String,
    @SerializedName("counts")        val counts: List<PortfolioCount>,
    @SerializedName("investAmount")  val investAmount: Long,
    @SerializedName("createdAt")     val createdAt: String,
    @SerializedName("portfolioType") val portfolioType: String  // "LUMP_SUM" or "REGULAR_SAVING"
)

data class PortfolioCount(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("counts") val counts: Double,
    @SerializedName("etfName") val etfName: String,
)