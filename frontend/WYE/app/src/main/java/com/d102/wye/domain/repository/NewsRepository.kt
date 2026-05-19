package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.NewsDetail
import com.d102.wye.domain.model.NewsPage
import com.d102.wye.domain.model.PortfolioNewsItem

interface NewsRepository {
    /** 뉴스 목록을 조회한다. */
    suspend fun getNewsList(category: String? = null, lastId: Long? = null): BaseResult<NewsPage>

    /** 뉴스 상세를 조회한다. */
    suspend fun getNewsDetail(newsId: Long): BaseResult<NewsDetail>

    /** 포트폴리오 관련 뉴스를 조회한다. */
    suspend fun getPortfolioNews(portfolioId: Long): BaseResult<List<PortfolioNewsItem>>

    /** 키워드로 뉴스를 검색한다. */
    suspend fun searchNews(keyword: String): BaseResult<NewsPage>
}
