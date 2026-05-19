package com.d102.wye.presentation.simulation.progress

import com.d102.wye.domain.model.EtfCluster
import com.d102.wye.domain.model.Portfolio

/**
 * 시뮬레이션 화면 전용 UI 모델
 */
data class PortfolioItem(
    val ticker: String,
    val name: String,
    val weight: Int,
    val per: Double = 0.0,
    val pbr: Double = 0.0,
    val roe: Double = 0.0,
    val currentPrice: Long = 0L,
    val sectors: List<EtfCluster> = emptyList()
)

/** presentation → domain 변환 */
fun PortfolioItem.toDomain(): Portfolio = Portfolio(
    ticker = ticker,
    name = name,
    weightPercent = weight
)

/** presentation 리스트 → domain 리스트 변환 */
fun List<PortfolioItem>.toDomain(): List<Portfolio> = map { it.toDomain() }