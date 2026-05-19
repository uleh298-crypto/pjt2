package com.d102.wye.domain.model

data class EtfMonthlyDividend(
    val month: String,  // "2026-01"
    val dividend: Long  // 해당 월 배당금 (원)
)

data class EtfDividendHistory(
    val etfId: Long,
    val etfName: String,
    val ticker: String,  // ticker별 구분용
    val dividends: List<EtfMonthlyDividend>
)