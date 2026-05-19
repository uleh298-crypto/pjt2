package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PresetDetailResponse(
    @SerializedName("presetId") val presetId: Int,
    @SerializedName("presetName") val presetName: String,
    @SerializedName("description") val description: String,
    @SerializedName("imageTag") val imageTag: String,
    @SerializedName("presetResponseList") val presetResponseList: List<PresetEtfResponse>
)

data class PresetEtfResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("name") val name: String
)
