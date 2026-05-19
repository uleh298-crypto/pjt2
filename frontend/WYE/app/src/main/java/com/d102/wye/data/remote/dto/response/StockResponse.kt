package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// GET /api/v1/stocks/{ticker}
data class StockDetailResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("stockName") val stockName: String,
    @SerializedName("marketCapitalization") val marketCapitalization: Long,
    @SerializedName("currentPrice") val currentPrice: Long,
    @SerializedName("dailyFluctuation") val dailyFluctuation: Double,
    @SerializedName("description") val description: String,
)

// GET /api/v1/stocks/{ticker}/etfs
data class StockEtfResponse(
    @SerializedName("etfName") val etfName: String,
    @SerializedName("manager") val manager: String,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("ratio") val ratio: Double,
)

// GET /api/v1/stocks/{ticker}/related
data class RelatedStockResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("companyName") val companyName: String,
    @SerializedName("industryCode") val industryCode: String,
    @SerializedName("industryName") val industryName: String,
    @SerializedName("relationType") val relationType: String,
    @SerializedName("logoUrl") val logoUrl: String,
)
