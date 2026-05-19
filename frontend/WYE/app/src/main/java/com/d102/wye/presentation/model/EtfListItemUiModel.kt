package com.d102.wye.presentation.model

data class EtfListItemUiModel(
    val etfId: Long = 0L,
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
    val isLiked: Boolean,
)
