package com.d102.wye.domain.model

data class EtfPricePoint(
    val date: String,        // "2026-01-15"
    val stockPrice: Long,    // 종가 (원)
    val dailyReturn: Double  // 전일 대비 수익률 (%)
)

data class EtfPriceHistory(
    val ticker: String,
    val content: List<EtfPricePoint>,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean
)