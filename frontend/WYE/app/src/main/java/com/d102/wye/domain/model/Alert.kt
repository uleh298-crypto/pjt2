package com.d102.wye.domain.model

data class Alert(
    val id: Long,
    val alertTypeCode: String,
    val alertTypeName: String,
    val category: String,
    val referenceType: String?,
    val referenceId: Long?,
    val referenceTicker: String?,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
)

data class AlertSetting(
    val settingGroup: String,
    val groupName: String,
    val description: String,
    val isEnabled: Boolean,
)
