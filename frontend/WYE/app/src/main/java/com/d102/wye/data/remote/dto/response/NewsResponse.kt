package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NewsListResponse(
    @SerializedName("news")
    val news: List<NewsItemResponse>,
    @SerializedName("hasMore")
    val hasMore: Boolean,
    @SerializedName("nextCursor")
    val nextCursor: Long?,
)

data class NewsItemResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("categoryCode")
    val categoryCode: String,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("publishedAt")
    val publishedAt: String
)

data class NewsDetailResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("sourceUrl")
    val sourceUrl: String,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("categoryCode")
    val categoryCode: String,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("keywords")
    val keywords: List<String>,
    @SerializedName("aiSummary")
    val aiSummary: List<String>,
    @SerializedName("relatedEtfs")
    val relatedEtfs: List<RelatedEtfResponse>
)

data class PortfolioNewsResponse(
    @SerializedName("portfolioId") val portfolioId: Long,
    @SerializedName("portfolioName") val portfolioName: String,
    @SerializedName("news") val news: List<PortfolioNewsItemResponse>,
    @SerializedName("updatedAt") val updatedAt: String,
)

data class PortfolioNewsItemResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("source") val source: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("publishedAt") val publishedAt: String,
)

data class RelatedEtfResponse(
    @SerializedName("etfId")
    val etfId: Long,
    @SerializedName("ticker")
    val ticker: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("manager")
    val manager: String,
    @SerializedName("changeRate")
    val changeRate: Double
)
