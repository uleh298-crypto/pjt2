package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfFundamentals
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.state.InvestmentType
import javax.inject.Inject

class RunSimulationUseCase @Inject constructor(
    private val calculateBacktest: CalculateBacktestUseCase,
    private val calculateWeightedFundamentals: CalculateWeightedFundamentalsUseCase
) {

    data class Params(
        val portfolios: List<Portfolio>,
        val investmentAmount: Long,
        val investmentType: InvestmentType,
        val periodMonths: Int,
        val priceHistories: Map<String, EtfPriceHistory>,
        val fundamentalsMap: Map<String, EtfFundamentals> = emptyMap(),
        val startDate: String? = null,
        val endDate: String? = null
    )

    suspend operator fun invoke(params: Params): BaseResult<SimulationResult> {
        if (params.priceHistories.isEmpty()) {
            return BaseResult.Error(
                ApiError(code = -1, message = "가격 이력 데이터가 없습니다")
            )
        }

        val backtestResult = calculateBacktest(
            portfolios = params.portfolios,
            priceHistories = params.priceHistories,
            investmentAmount = params.investmentAmount,
            investmentType = params.investmentType,
            periodMonths = params.periodMonths
        )

        // per/pbr/roe 가중평균 계산
        val fundamentals = calculateWeightedFundamentals(
            portfolios = params.portfolios,
            fundamentalsMap = params.fundamentalsMap
        )

        return BaseResult.Success(
            SimulationResult(
                backtestPoints = backtestResult.points,
                fundamentals = fundamentals,
                expectedAnnualDividend = 0L,   // TODO: 배당 API 연동 후 채우기
                expectedMonthlyDividend = 0L,
                estimatedFinalValue = backtestResult.estimatedFinalValue,
                totalReturn = backtestResult.totalReturn,
                totalInvestment = backtestResult.totalInvestment
            )
        )
    }
}