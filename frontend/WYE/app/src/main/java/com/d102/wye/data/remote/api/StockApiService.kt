package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.RelatedStockResponse
import com.d102.wye.data.remote.dto.response.StockDetailResponse
import com.d102.wye.data.remote.dto.response.StockEtfResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StockApiService {

    // GET /api/v1/stocks/{ticker}
    @GET("stocks/{ticker}")
    suspend fun getStockDetail(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<StockDetailResponse>>

    // GET /api/v1/stocks/{ticker}/etfs
    @GET("stocks/{ticker}/etfs")
    suspend fun getContainedEtfs(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<List<StockEtfResponse>>>

    // GET /api/v1/stocks/{ticker}/related
    @GET("stocks/{ticker}/related")
    suspend fun getRelatedStocks(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<List<RelatedStockResponse>>>

    // GET /api/v1/stocks/{ticker}/tags
    @GET("stocks/{ticker}/tags")
    suspend fun getTags(
        @Path("ticker") ticker: String,
    ): Response<BaseResponse<List<String>>>
}
