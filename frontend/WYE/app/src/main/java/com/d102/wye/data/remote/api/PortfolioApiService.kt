package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.data.remote.dto.request.UpdatePortfolioRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.PortfolioDetail
import com.d102.wye.data.remote.dto.response.PortfolioIssueDto
import com.d102.wye.data.remote.dto.response.PortfolioListItemDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PortfolioApiService {

    @POST("portfolios")
    suspend fun savePortfolio(
        @Body request: SavePortfolioRequest
    ): Response<BaseResponse<Unit>>

    @GET("portfolios")
    suspend fun getPortfolioList(): Response<BaseResponse<List<PortfolioListItemDto>>>

    @GET("portfolios/{portfolioId}")
    suspend fun getPortfolioDetail(
        @Path("portfolioId") portfolioId: Long
    ): Response<BaseResponse<PortfolioDetail>>

    @PUT("portfolios")
    suspend fun updatePortfolio(
        @Body request: UpdatePortfolioRequest
    ): Response<BaseResponse<Unit>>

    @DELETE("portfolios/{portfolioId}")
    suspend fun deletePortfolio(
        @Path("portfolioId") portfolioId: Long
    ): Response<BaseResponse<Unit>>

    @GET("portfolios/{portfolioId}/issues")
    suspend fun getPortfolioIssues(
        @Path("portfolioId") portfolioId: Long
    ): Response<BaseResponse<List<PortfolioIssueDto>>>
}
