package com.d102.wye.presentation.model

data class NewsUiModel(
    val id: Long,
    val title: String,
    val summary: String,        // AI 요약
    val publishedAt: String
)