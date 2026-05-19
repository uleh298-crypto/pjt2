package com.d102.wye.data.remote.api

import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.NewsDetailResponse
import com.d102.wye.data.remote.dto.response.NewsListResponse
import com.d102.wye.data.remote.dto.response.PortfolioNewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {

    /**
     * 뉴스 목록을 조회 (카테고리를 넘기면 해당 카테고리만 조회)
     * */
    @GET("news")
    suspend fun getNewsList(
        @Query("category") category: String? = null,
        @Query("lastId") lastId: Long? = null,
        @Query("size") size: Int = 20,
    ): Response<BaseResponse<NewsListResponse>>

    /**
     * 뉴스 상세 조회
     * */
    @GET("news/{newsId}")
    suspend fun getNewsDetail(
        @Path("newsId") newsId: Long
    ): Response<BaseResponse<NewsDetailResponse>>

    /** 포트폴리오 관련 뉴스를 조회한다. */
    @GET("news/portfolio/{portfolioId}")
    suspend fun getPortfolioNews(
        @Path("portfolioId") portfolioId: Long
    ): Response<BaseResponse<PortfolioNewsResponse>>

    /** 키워드로 뉴스를 검색한다. */
    @GET("news/search")
    suspend fun searchNews(
        @Query("keyword") keyword: String,
    ): Response<BaseResponse<NewsListResponse>>
}
