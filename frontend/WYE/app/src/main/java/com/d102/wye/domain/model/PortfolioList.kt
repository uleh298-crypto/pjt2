package com.d102.wye.domain.model

data class PortfolioListItem(
    val portfolioId: Long,
    val title: String,
    val createdAt: String,
    val etfList: List<PortfolioEtf>,
    val totalReturn: Double,
    val isMyData: Boolean
)

data class PortfolioEtf(
    val ticker: String,
    val name: String
)