package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class EtfPriceHistoryRequest(
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate")   val endDate: String? = null
)

data class EtfDividendHistoryRequest(
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate")   val endDate: String? = null
)