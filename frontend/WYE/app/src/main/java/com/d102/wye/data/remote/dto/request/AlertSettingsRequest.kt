package com.d102.wye.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class AlertSettingsRequest(
    @SerializedName("settings")
    val settings: List<AlertSettingItemRequest>,
)

data class AlertSettingItemRequest(
    @SerializedName("settingGroup")
    val settingGroup: String,
    @SerializedName("isEnabled")
    val isEnabled: Boolean,
)
