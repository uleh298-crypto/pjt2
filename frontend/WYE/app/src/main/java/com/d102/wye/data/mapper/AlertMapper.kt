package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.AlertItemResponse
import com.d102.wye.data.remote.dto.response.AlertSettingItemResponse
import com.d102.wye.domain.model.Alert
import com.d102.wye.domain.model.AlertSetting

fun AlertItemResponse.toDomain() = Alert(
    id = id,
    alertTypeCode = alertTypeCode,
    alertTypeName = alertTypeName,
    category = category,
    referenceType = referenceType,
    referenceId = referenceId,
    referenceTicker = referenceTicker,
    title = title,
    message = message,
    isRead = isRead,
    createdAt = createdAt,
)

fun AlertSettingItemResponse.toDomain() = AlertSetting(
    settingGroup = settingGroup,
    groupName = groupName,
    description = description,
    isEnabled = isEnabled,
)
