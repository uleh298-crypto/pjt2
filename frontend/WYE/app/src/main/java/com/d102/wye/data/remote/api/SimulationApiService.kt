package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.AiReviewRequest
import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.data.remote.dto.response.AiReviewResponse
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.EtfDividendHistoryResponse
import com.d102.wye.data.remote.dto.response.EtfPriceHistoryResponse
import com.d102.wye.data.remote.dto.response.PresetDetailResponse
import com.d102.wye.data.remote.dto.response.PresetListItemResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SimulationApiService {

    /**
     * ETF 가격 이력 조회
     */
    @GET("etfs/{ticker}/price-history")
    suspend fun getEtfPriceHistory(
        @Path("ticker") ticker: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): BaseResponse<EtfPriceHistoryResponse>

    /**
     * ETF 월별 배당금 이력 조회
     */
    @GET("etfs/{ticker}/dividends")
    suspend fun getEtfDividendHistory(
        @Path("ticker") ticker: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): BaseResponse<EtfDividendHistoryResponse>

    @POST("ai/portfolio/review")
    suspend fun getAiPortfolioReview(
        @Body request: AiReviewRequest
    ): BaseResponse<AiReviewResponse>

    @GET("presets")
    suspend fun getPresetList(): BaseResponse<List<PresetListItemResponse>>

    @GET("presets/{presetId}")
    suspend fun getPresetDetail(
        @Path("presetId") presetId: Int
    ): BaseResponse<PresetDetailResponse>

}