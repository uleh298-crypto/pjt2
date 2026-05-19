package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AlertListResponse(
    @SerializedName("alerts")
    val alerts: List<AlertItemResponse>,
    @SerializedName("unreadCount")
    val unreadCount: Int,
)

data class AlertItemResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("alertTypeCode")
    val alertTypeCode: String,
    @SerializedName("alertTypeName")
    val alertTypeName: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("referenceType")
    val referenceType: String?,
    @SerializedName("referenceId")
    val referenceId: Long?,
    @SerializedName("referenceTicker")
    val referenceTicker: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
)

data class UnreadCountResponse(
    @SerializedName("count")
    val count: Int,
)

data class AlertSettingsResponse(
    @SerializedName("settings")
    val settings: List<AlertSettingItemResponse>,
)

data class AlertSettingItemResponse(
    @SerializedName("settingGroup")
    val settingGroup: String,
    @SerializedName("groupName")
    val groupName: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("isEnabled")
    val isEnabled: Boolean,
)
