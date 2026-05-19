package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class EtfListRequest(
    @SerializedName("riskType") val riskType: String? = null,
    @SerializedName("strategy") val strategy: String? = null,
    @SerializedName("sector") val sector: String? = null,
    @SerializedName("dividendYield") val dividendYield: Double? = null,
    @SerializedName("dividendFrequency") val dividendFrequency: String? = null,
    @SerializedName("isDerivatives") val isDerivatives: Boolean? = null,
    @SerializedName("isLeverage") val isLeverage: Boolean? = null,
    @SerializedName("isInverse") val isInverse: Boolean? = null,
    @SerializedName("perLow") val perLow: Double? = null,
    @SerializedName("perHigh") val perHigh: Double? = null,
    @SerializedName("pbrLow") val pbrLow: Double? = null,
    @SerializedName("pbrHigh") val pbrHigh: Double? = null,
    @SerializedName("roeLow") val roeLow: Double? = null,
    @SerializedName("roeHigh") val roeHigh: Double? = null,
    @SerializedName("commission") val commission: Double? = null,
    @SerializedName("aum") val aum: Long? = null,
    @SerializedName("sortedBy") val sortedBy: String? = null,
    @SerializedName("searchName") val searchName: String? = null,
    @SerializedName("isFavorite") val isFavorite: Boolean? = null,
)
