package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SavePortfolioRequest(
    @SerializedName("portfolioName") val portfolioName: String,
    @SerializedName("portfolioType") val portfolioType: String,  // "LUMP_SUM" or "REGULAR_SAVING"
    @SerializedName("investAmount") val investAmount: Long,    // 적립형: 월납입금 / 거치형: 총투자금
    @SerializedName("investPeriod") val investPeriod: Int,
    @SerializedName("etfs") val etfs: List<EtfCount>
)

data class EtfCount(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("counts") val counts: Double  // (investAmount × weight / 100) / currentPrice
)