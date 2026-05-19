package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// POST /api/v1/etfs 페이지 응답
data class EtfPageResponse(
    @SerializedName("content") val content: List<EtfListItemResponse>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("last") val last: Boolean,
)

data class EtfListItemResponse(
    @SerializedName("etfId") val etfId: Long,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("etfName") val etfName: String,
    @SerializedName("etfPrice") val etfPrice: Long,
    @SerializedName("dailyReturn") val dailyReturn: Double,
    @SerializedName("dailyFluctuation") val dailyFluctuation: Long,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("riskType") val riskType: String,
)


