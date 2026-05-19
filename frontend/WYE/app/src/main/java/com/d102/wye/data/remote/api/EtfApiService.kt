package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.EtfListRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.EtfClusterDataResponse
import com.d102.wye.data.remote.dto.response.EtfDetailResponse
import com.d102.wye.data.remote.dto.response.EtfMarketDataResponse
import com.d102.wye.data.remote.dto.response.EtfPageResponse
import com.d102.wye.data.remote.dto.response.EtfPriceHistoryPageResponse
import com.d102.wye.data.remote.dto.response.TopVolumeEtfResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EtfApiService {

    // 홈 거래량 TOP 10 ETF 조회 GET /api/v1/etfs
    @GET("etfs")
    suspend fun getTopVolumeEtfs(): Response<BaseResponse<List<TopVolumeEtfResponse>>>

    // ETF 목록 조회 POST /api/v1/etfs
    @POST("etfs")
    suspend fun getEtfList(
        @Body request: EtfListRequest,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<BaseResponse<EtfPageResponse>>

    // 관심 ETF 토글
    @POST("etf/{ticker}/like")
    suspend fun toggleLike(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<Boolean>>

    // ETF 단건 조회 GET /api/v1/etfs/{ticker}
    @GET("etfs/{ticker}")
    suspend fun getEtfDetail(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<EtfDetailResponse>>

    // ETF 클러스터 조회 GET /api/v1/etfs/{ticker}/clusters
    // data = { englishName, sectors[], influentialStocks[] }
    @GET("etfs/{ticker}/clusters")
    suspend fun getEtfCluster(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<EtfClusterDataResponse>>

    // ETF 시장 데이터 조회 GET /api/v1/etfs/{ticker}/market-data
    @GET("etfs/{ticker}/market-data")
    suspend fun getMarketData(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<EtfMarketDataResponse>>

    // ETF 가격 이력 조회 GET /api/v1/etfs/{ticker}/price-history
    @GET("etfs/{ticker}/price-history")
    suspend fun getEtfPriceHistory(
        @Path("ticker") ticker: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("validDateRange") validDateRange: Boolean = true,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 300,
    ): Response<BaseResponse<EtfPriceHistoryPageResponse>>
}
