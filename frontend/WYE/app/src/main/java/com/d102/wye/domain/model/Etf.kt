package com.d102.wye.domain.model

data class EtfPage(
    val items: List<Etf>,
    val isLast: Boolean,
)

// POST /api/v1/etfs 리스트 API 응답 항목
data class Etf(
    val etfId: Long,
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
    val isFavorite: Boolean,
)
