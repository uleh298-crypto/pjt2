package com.d102.wye.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AiReviewResponse(
    @SerializedName("reviewId") val reviewId: Long,
    @SerializedName("headline") val headline: String,
    @SerializedName("subHeadline") val subHeadline: String,
    @SerializedName("keywords") val keywords: List<String>,
    @SerializedName("analysis") val analysis: String,
    @SerializedName("llmModel") val llmModel: String,
    @SerializedName("createdAt") val createdAt: String
)
