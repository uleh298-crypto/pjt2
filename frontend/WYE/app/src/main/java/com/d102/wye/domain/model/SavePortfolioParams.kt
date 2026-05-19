package com.d102.wye.domain.model

import com.d102.wye.domain.state.InvestmentType

data class SavePortfolioParams(
    val portfolioName: String,
    val investType: InvestmentType,
    val investAmount: Long,    // 적립형: 월납입금 / 거치형: 총투자금
    val investPeriod: Int,
    val etfs: List<EtfCountItem>
)

data class EtfCountItem(
    val ticker: String,
    val counts: Double  // (investAmount × weight / 100) / currentPrice
)

