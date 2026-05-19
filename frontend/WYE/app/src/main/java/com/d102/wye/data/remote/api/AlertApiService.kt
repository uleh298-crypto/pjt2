package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.AlertSettingsRequest
import com.d102.wye.data.remote.dto.response.AlertListResponse
import com.d102.wye.data.remote.dto.response.AlertSettingsResponse
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AlertApiService {

    @GET("alerts")
    suspend fun getAlerts(
        @Query("category") category: String = "all",
    ): Response<BaseResponse<AlertListResponse>>

    @GET("alerts/unread/count")
    suspend fun getUnreadCount(): Response<BaseResponse<UnreadCountResponse>>

    @PUT("alerts/{alertId}/read")
    suspend fun markAsRead(
        @Path("alertId") alertId: Long,
    ): Response<BaseResponse<Unit>>

    @GET("alerts/settings")
    suspend fun getSettings(): Response<BaseResponse<AlertSettingsResponse>>

    @PUT("alerts/settings")
    suspend fun updateSettings(
        @Body request: AlertSettingsRequest,
    ): Response<BaseResponse<AlertSettingsResponse>>
}
