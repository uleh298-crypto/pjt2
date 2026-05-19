package com.d102.wye.domain.model

data class EtfLikeData(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
)
