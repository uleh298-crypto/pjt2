package com.d102.wye.domain.model

import com.d102.wye.domain.state.InvestmentType

data class PortfolioDetail(
    val portfolioId: Long,
    val portfolioName: String,
    val counts: List<PortfolioCount>,
    val investAmount: Long,
    val createdAt: String,
    val portfolioType: InvestmentType
)

data class PortfolioCount(
    val ticker: String,
    val counts: Double,
    val etfName: String
)