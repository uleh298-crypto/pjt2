package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.model.EtfFundamentals
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.model.WeightedFundamentals
import javax.inject.Inject
import kotlin.math.roundToLong

class CalculateWeightedFundamentalsUseCase @Inject constructor() {

    operator fun invoke(
        portfolios: List<Portfolio>,
        fundamentalsMap: Map<String, EtfFundamentals>
    ): WeightedFundamentals {
        var per = 0.0
        var pbr = 0.0
        var roe = 0.0

        portfolios.forEach { portfolio ->
            val f = fundamentalsMap[portfolio.ticker] ?: return@forEach
            val weight = portfolio.weightPercent / 100.0
            per += f.per * weight
            pbr += f.pbr * weight
            roe += f.roe * weight
        }

        return WeightedFundamentals(
            per = per.roundToOneDecimal(),
            pbr = pbr.roundToOneDecimal(),
            roe = roe.roundToOneDecimal()
        )
    }

    private fun Double.roundToOneDecimal(): Double =
        (this * 10).roundToLong() / 10.0
}