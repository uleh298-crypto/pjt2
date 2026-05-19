package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.model.EtfFundamentals
import com.d102.wye.domain.model.Portfolio
import javax.inject.Inject
import kotlin.math.roundToLong

class CalculateExpectedDividendUseCase @Inject constructor() {

    data class Result(
        val annualDividend: Long,
        val monthlyDividend: Long
    )

    operator fun invoke(
        portfolios: List<Portfolio>,
        investmentAmount: Long,
        fundamentalsMap: Map<String, EtfFundamentals>
    ): Result {
        val weightedYield = portfolios.sumOf { portfolio ->
            val f = fundamentalsMap[portfolio.ticker] ?: return@sumOf 0.0
            (portfolio.weightPercent / 100.0) * f.annualDividendYield
        }

        val annualDividend = (investmentAmount * weightedYield / 100.0).roundToLong()

        return Result(
            annualDividend = annualDividend,
            monthlyDividend = annualDividend / 12
        )
    }
}