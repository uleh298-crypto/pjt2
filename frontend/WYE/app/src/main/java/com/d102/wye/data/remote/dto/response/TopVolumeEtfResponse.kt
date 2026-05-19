package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class TopVolumeEtfResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String,
    @SerializedName("dailyReturn") val dailyReturn: Double,
    @SerializedName("volume") val volume: Long
)
