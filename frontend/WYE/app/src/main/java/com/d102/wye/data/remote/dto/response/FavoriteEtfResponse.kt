package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class FavoriteEtfListResponse(
    @SerializedName("favorites") val favorites: List<FavoriteEtfResponse>,
    @SerializedName("totalCount") val totalCount: Int
)

data class FavoriteEtfResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("riskType") val riskType: String?,
    @SerializedName("assetManager") val assetManager: String,
    @SerializedName("currentPrice") val currentPrice: Double,
    @SerializedName("changeRate") val changeRate: Double,
    @SerializedName("favoritedAt") val favoritedAt: String
)
