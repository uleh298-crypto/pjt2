package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.NewsDetailResponse
import com.d102.wye.data.remote.dto.response.NewsItemResponse
import com.d102.wye.data.remote.dto.response.NewsListResponse
import com.d102.wye.data.remote.dto.response.PortfolioNewsItemResponse
import com.d102.wye.data.remote.dto.response.RelatedEtfResponse
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.NewsDetail
import com.d102.wye.domain.model.NewsPage
import com.d102.wye.domain.model.PortfolioNewsItem
import com.d102.wye.domain.model.RelatedEtf

/** 서버 뉴스 아이템 DTO를 앱에서 사용하는 도메인 모델로 변환한다. */
fun NewsItemResponse.toDomain() = News(
    id = id,
    title = title,
    source = source,
    thumbnailUrl = thumbnailUrl,
    categoryCode = categoryCode,
    categoryName = categoryName,
    publishedAt = publishedAt
)

/** 서버 뉴스 목록 응답을 페이지 도메인 모델로 변환한다. */
fun NewsListResponse.toDomain() = NewsPage(
    news = news.map { it.toDomain() },
    hasMore = hasMore,
    nextCursor = nextCursor,
)

/** 서버 뉴스 상세 DTO를 앱에서 사용하는 상세 도메인 모델로 변환한다. */
fun NewsDetailResponse.toDomain() = NewsDetail(
    id = id,
    title = title,
    content = content,
    source = source,
    sourceUrl = sourceUrl,
    thumbnailUrl = thumbnailUrl,
    publishedAt = publishedAt,
    categoryCode = categoryCode,
    categoryName = categoryName,
    keywords = keywords,
    aiSummary = aiSummary,
    relatedEtfs = relatedEtfs.map { it.toDomain() }
)

/** 포트폴리오 뉴스 아이템 DTO를 도메인 모델로 변환한다. */
fun PortfolioNewsItemResponse.toDomain() = PortfolioNewsItem(
    id = id,
    title = title,
    summary = summary,
    source = source,
    thumbnailUrl = thumbnailUrl,
    publishedAt = publishedAt,
)

/** 관련 ETF 응답 DTO를 상세 화면에서 사용하는 도메인 모델로 변환한다. */
fun RelatedEtfResponse.toDomain() = RelatedEtf(
    etfId = etfId,
    ticker = ticker,
    name = name,
    manager = manager,
    changeRate = changeRate
)
