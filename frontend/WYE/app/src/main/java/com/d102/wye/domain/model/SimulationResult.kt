package com.d102.wye.domain.model

data class SimulationResult(
    val backtestPoints: List<BacktestPoint>,
    val fundamentals: WeightedFundamentals,
    val expectedAnnualDividend: Long,
    val expectedMonthlyDividend: Long,
    val estimatedFinalValue: Long,
    val totalReturn: Double,   // %
    val totalInvestment: Long  // 원
)