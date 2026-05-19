package com.d102.wye.domain.model

/**
 * 포트폴리오 구성 종목
 */
data class Portfolio(
    val ticker: String,
    val name: String,
    val weightPercent: Int  // 0~100
)