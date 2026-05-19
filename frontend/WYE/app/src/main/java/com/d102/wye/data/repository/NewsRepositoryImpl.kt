package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.NewsApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.NewsDetail
import com.d102.wye.domain.model.NewsPage
import com.d102.wye.domain.model.PortfolioNewsItem
import com.d102.wye.domain.repository.NewsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : BaseRepository(), NewsRepository {

    /** 뉴스 목록 API를 호출하고 응답 DTO를 도메인 모델로 변환한다. */
    override suspend fun getNewsList(category: String?, lastId: Long?): BaseResult<NewsPage> {
        return safeApiCall {
            newsApiService.getNewsList(category = category, lastId = lastId)
        }.map { it.toDomain() }
    }

    /** 뉴스 상세 API를 호출하고 응답 DTO를 도메인 모델로 변환한다. */
    override suspend fun getNewsDetail(newsId: Long): BaseResult<NewsDetail> {
        return safeApiCall {
            newsApiService.getNewsDetail(newsId = newsId)
        }.map { it.toDomain() }
    }

    /** 포트폴리오 관련 뉴스 API를 호출하고 응답 DTO를 도메인 모델로 변환한다. */
    override suspend fun getPortfolioNews(portfolioId: Long): BaseResult<List<PortfolioNewsItem>> {
        return safeApiCall {
            newsApiService.getPortfolioNews(portfolioId = portfolioId)
        }.map { it.news.map { item -> item.toDomain() } }
    }

    /** 뉴스 검색 API를 호출하고 응답 DTO를 도메인 모델로 변환한다. */
    override suspend fun searchNews(keyword: String): BaseResult<NewsPage> {
        return safeApiCall {
            newsApiService.searchNews(keyword = keyword)
        }.map { it.toDomain() }
    }
}
