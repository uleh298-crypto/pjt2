package com.d102.wye.domain.usecase.portfolio

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.PortfolioCount
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * 포트폴리오 상세 / 비교 화면용 차트 계산
 *
 * y축 = 수익률 (%)
 *   첫날 평가금액 기준 → (당일 평가금액 - 첫날) / 첫날 × 100
 *   → 보유 주수/시드머니 무관하게 공정 비교 가능
 *
 * 두 구간:
 * - 최근 성과: createdAt ~ 오늘  (recentPoints)
 * - 과거 1년: createdAt-1년 ~ createdAt (pastPoints)
 */
class CalculatePortfolioChartUseCase @Inject constructor() {

    data class Result(
        val recentPoints: List<BacktestPoint>,   // y = 수익률(%)
        val pastPoints: List<BacktestPoint>,     // y = 수익률(%)
        val recentReturn: Double,                // 최근 수익률 (%)
        val pastReturn: Double,                  // 과거 1년 수익률 (%)
        val estimatedFinalValue: Long            // 현재 절대 평가금액 (원) - 요약 카드용
    )

    operator fun invoke(
        counts: List<PortfolioCount>,
        priceHistories: Map<String, EtfPriceHistory>,
        createdAt: String   // "2026-03-15"
    ): Result {
        if (counts.all { it.counts == 0.0 }) {
            return Result(emptyList(), emptyList(), 0.0, 0.0, 0L)
        }

        // 전체 날짜 교집합 (오름차순)
        val allDates: List<String> = priceHistories.values
            .map { it.content.map { p -> p.date }.toSet() }
            .reduceOrNull { acc, set -> acc.intersect(set) }
            ?.sorted()
            ?: return Result(emptyList(), emptyList(), 0.0, 0.0, 0L)

        if (allDates.isEmpty()) return Result(emptyList(), emptyList(), 0.0, 0.0, 0L)

        // 날짜 → 주가 맵
        val priceMap: Map<String, Map<String, Double>> =
            priceHistories.mapValues { (_, history) ->
                history.content.associate { it.date to it.stockPrice.toDouble() }
            }

        // 절대 평가금액 계산
        fun portfolioValue(date: String): Double =
            counts.sumOf { count ->
                val price = priceMap[count.ticker]?.get(date) ?: 0.0
                count.counts * price
            }

        // 수익률(%) 변환 함수
        fun toReturnRate(value: Double, baseValue: Double): Double =
            if (baseValue > 0) (value - baseValue) / baseValue * 100.0 else 0.0

        // ── 최근 구간: createdAt ~ 오늘 ──────────────────────────────────────
        val recentDates = allDates.filter { it >= createdAt }
        val recentPoints = if (recentDates.isNotEmpty()) {
            val baseValue = portfolioValue(recentDates.first())
            recentDates.map { date ->
                BacktestPoint(
                    date = date,
                    value = toReturnRate(portfolioValue(date), baseValue)
                )
            }
        } else emptyList()

        // ── 과거 구간: createdAt-1년 ~ createdAt ─────────────────────────────
        val pastStartDate = createdAt.toLocalDateMinusOneYear()
        val pastDates = allDates.filter { it in pastStartDate..createdAt }
        val pastPoints = if (pastDates.isNotEmpty()) {
            val baseValue = portfolioValue(pastDates.first())
            pastDates.map { date ->
                BacktestPoint(
                    date = date,
                    value = toReturnRate(portfolioValue(date), baseValue)
                )
            }
        } else emptyList()

        // 수익률 = 마지막 포인트의 value (이미 % 단위)
        val recentReturn = recentPoints.lastOrNull()?.value ?: 0.0
        val pastReturn = pastPoints.lastOrNull()?.value ?: 0.0

        // 절대 평가금액은 요약 카드용으로만 유지
        val estimatedFinalValue = recentDates.lastOrNull()
            ?.let { portfolioValue(it).roundToLong() } ?: 0L

        return Result(
            recentPoints = recentPoints,
            pastPoints = pastPoints,
            recentReturn = recentReturn,
            pastReturn = pastReturn,
            estimatedFinalValue = estimatedFinalValue
        )
    }
}

private fun String.toLocalDateMinusOneYear(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val year = parts[0].toIntOrNull() ?: return this
    return "${year - 1}-${parts[1]}-${parts[2]}"
}