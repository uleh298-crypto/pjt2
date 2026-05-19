package com.d102.wye.presentation.simulation.model

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.simulation.progress.SectorWeightUiModel

/**
 * 시뮬레이션 화면 전용 UI 모델
 */
data class SimulationUiModel(
    // 요약 카드
    val estimatedFinalAsset: String,  // "1억 2,345만원"
    val netProfit: String,            // "+2,345만원" or "-500만원"
    val yieldRate: String,            // "+12.34%" or "-5.67%"
    val totalInvestment: String,      // "3,600만원"

    // 펀더멘털
    val per: String,
    val pbr: String,
    val roe: String,

    // 배당금
    val expectedAnnualDividend: String,
    val expectedMonthlyDividend: String,

    // 차트 데이터
    val backtestPoints: List<BacktestPoint>,
    val investmentType: InvestmentType,
    val isPositiveReturn: Boolean,
    val sectorWeights: List<SectorWeightUiModel> = emptyList()
)

fun SimulationResult.toUiModel(
    investmentType: InvestmentType,
    sectorWeights: List<SectorWeightUiModel> = emptyList()
): SimulationUiModel {
    val isPositive = totalReturn >= 0.0
    val netProfitValue = estimatedFinalValue - totalInvestment

    return SimulationUiModel(
        estimatedFinalAsset = estimatedFinalValue.formatAmount(),
        netProfit = if (netProfitValue >= 0) "+${netProfitValue.formatFullAmount()}"
        else "-${kotlin.math.abs(netProfitValue).formatFullAmount()}",
        yieldRate = "${if (isPositive) "+" else ""}${String.format("%.2f", totalReturn)}%",
        totalInvestment = totalInvestment.formatAmount(),
        per = "${String.format("%.1f", fundamentals.per)}배",
        pbr = "${String.format("%.1f", fundamentals.pbr)}배",
        roe = "${String.format("%.1f", fundamentals.roe)}%",
        expectedAnnualDividend = expectedAnnualDividend.formatAmount(),
        expectedMonthlyDividend = expectedMonthlyDividend.formatAmount(),
        backtestPoints = backtestPoints,
        investmentType = investmentType,
        isPositiveReturn = isPositive,
        sectorWeights = sectorWeights
    )
}

private fun Long.formatAmount(): String {
    val absValue = kotlin.math.abs(this)

    return when {
        // 1천만원 이하 → 원 단위
        absValue < 10_000_000 -> {
            "%,d원".format(absValue)
        }

        // 1억원 이하 → 만원 단위
        absValue < 100_000_000 -> {
            val man = absValue / 10_000
            "%,d만원".format(man)
        }

        // 1조 이하 → 억 + 만원
        absValue < 1_000_000_000_000L -> {
            val eok = absValue / 100_000_000
            val remainder = (absValue % 100_000_000) / 10_000

            val eokStr = "%,d".format(eok)

            if (remainder > 0) {
                "${eokStr}억 ${"%,d".format(remainder)}만원"
            } else {
                "${eokStr}억"
            }
        }

        // 1조 이상 → 조 + 억
        else -> {
            val jo = absValue / 1_000_000_000_000L
            val remainder = (absValue % 1_000_000_000_000L) / 100_000_000

            val joStr = "%,d".format(jo)

            if (remainder > 0) {
                "${joStr}조 ${"%,d".format(remainder)}억"
            } else {
                "${joStr}조"
            }
        }
    }
}

private fun Long.formatFullAmount(): String {
    return "%,d".format(this)
}