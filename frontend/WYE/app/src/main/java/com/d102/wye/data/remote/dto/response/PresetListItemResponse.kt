package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PresetListItemResponse(
    @SerializedName("presetId") val presetId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("imageTag") val imageTag: String,
    @SerializedName("presetTagList") val presetTagList: List<String>
)