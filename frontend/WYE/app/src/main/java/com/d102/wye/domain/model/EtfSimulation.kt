package com.d102.wye.domain.model

data class EtfFundamentals(
    val ticker: String,
    val per: Double,               // Price-Earnings Ratio
    val pbr: Double,               // Price-Book Ratio
    val roe: Double,               // Return on Equity (%)
    val annualDividendYield: Double // 연간 배당수익률 (%)
)

/** 날짜별 포트폴리오 수익률/가치 포인트 */
data class BacktestPoint(
    val date: String,
    val value: Double
)

/** 가중 평균 펀더멘털 지표 */
data class WeightedFundamentals(
    val per: Double,
    val pbr: Double,
    val roe: Double
)