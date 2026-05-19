package com.d102.wye.domain.model

data class NewsPage(
    val news: List<News>,
    val hasMore: Boolean,
    val nextCursor: Long?,
)

data class News(
    val id: Long,
    val title: String,
    val source: String,
    val thumbnailUrl: String?,
    val categoryCode: String,
    val categoryName: String,
    val publishedAt: String
)

data class NewsDetail(
    val id: Long,
    val title: String,
    val content: String,
    val source: String,
    val sourceUrl: String,
    val thumbnailUrl: String?,
    val publishedAt: String,
    val categoryCode: String,
    val categoryName: String,
    val keywords: List<String>,
    val aiSummary: List<String>,
    val relatedEtfs: List<RelatedEtf>
)

data class PortfolioNewsItem(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val thumbnailUrl: String?,
    val publishedAt: String,
)

data class RelatedEtf(
    val etfId: Long,
    val ticker: String,
    val name: String,
    val manager: String,
    val changeRate: Double
)
