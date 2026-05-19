package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Alert
import com.d102.wye.domain.model.AlertSetting

interface AlertRepository {

    suspend fun getAlerts(category: String = "all"): BaseResult<List<Alert>>

    suspend fun getUnreadCount(): BaseResult<Int>

    suspend fun markAsRead(alertId: Long): BaseResult<Unit>

    suspend fun getSettings(): BaseResult<List<AlertSetting>>

    suspend fun updateSettings(settings: List<AlertSetting>): BaseResult<List<AlertSetting>>
}
