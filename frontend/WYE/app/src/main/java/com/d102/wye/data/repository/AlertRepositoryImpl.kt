package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.AlertApiService
import com.d102.wye.data.remote.dto.request.AlertSettingItemRequest
import com.d102.wye.data.remote.dto.request.AlertSettingsRequest
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.Alert
import com.d102.wye.domain.model.AlertSetting
import com.d102.wye.domain.repository.AlertRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val alertApiService: AlertApiService,
) : BaseRepository(), AlertRepository {

    override suspend fun getAlerts(category: String): BaseResult<List<Alert>> {
        return safeApiCall {
            alertApiService.getAlerts(category)
        }.map { it.alerts.map { alert -> alert.toDomain() } }
    }

    override suspend fun getUnreadCount(): BaseResult<Int> {
        return safeApiCall {
            alertApiService.getUnreadCount()
        }.map { it.count }
    }

    override suspend fun markAsRead(alertId: Long): BaseResult<Unit> {
        return safeApiCall {
            alertApiService.markAsRead(alertId)
        }
    }

    override suspend fun getSettings(): BaseResult<List<AlertSetting>> {
        return safeApiCall {
            alertApiService.getSettings()
        }.map { it.settings.map { setting -> setting.toDomain() } }
    }

    override suspend fun updateSettings(settings: List<AlertSetting>): BaseResult<List<AlertSetting>> {
        return safeApiCall {
            alertApiService.updateSettings(
                AlertSettingsRequest(
                    settings = settings.map {
                        AlertSettingItemRequest(
                            settingGroup = it.settingGroup,
                            isEnabled = it.isEnabled,
                        )
                    }
                )
            )
        }.map { it.settings.map { setting -> setting.toDomain() } }
    }
}
