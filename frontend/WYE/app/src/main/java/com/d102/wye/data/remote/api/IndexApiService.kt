package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.IndexPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IndexApiService {

    // GET /api/v1/index?marketType=KOSPI&page=0&size=1000
    @GET("index")
    suspend fun getIndex(
        @Query("marketType") marketType: String,
        @Query("page")       page:       Int = 0,
        @Query("size")       size:       Int = 1000,
    ): Response<BaseResponse<IndexPageResponse>>
}
