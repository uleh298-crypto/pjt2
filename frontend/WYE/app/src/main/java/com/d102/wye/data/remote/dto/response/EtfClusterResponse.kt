package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

// GET /api/v1/etfs/{ticker}/clusters 응답 (data 오브젝트)
data class EtfClusterDataResponse(
    @SerializedName("englishName") val englishName: String,
    @SerializedName("sectors") val sectors: List<EtfSectorResponse>,
    @SerializedName("influentialStocks") val influentialStocks: List<EtfInfluentialStockResponse>,
)

data class EtfSectorResponse(
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Double,
    @SerializedName("stocks") val stocks: List<EtfClusterStockResponse>,
    @SerializedName("aiAnalysis") val aiAnalysis: String?,
    @SerializedName("assetType") val assetType: String?,
)

data class EtfClusterStockResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("percentage") val percentage: Double,
)

data class EtfInfluentialStockResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("currentPrice") val currentPrice: Long,
    @SerializedName("changeRate") val changeRate: Double,
)
