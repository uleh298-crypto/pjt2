package com.whatsyouretf.userservice.domain.simulation.dto;

import com.whatsyouretf.userservice.domain.simulation.entity.RebalancingCycle;
import com.whatsyouretf.userservice.domain.simulation.entity.Simulation;
import com.whatsyouretf.userservice.domain.simulation.entity.SimulationEtfPerformance;
import com.whatsyouretf.userservice.domain.simulation.entity.SimulationMonthlyReturn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시뮬레이션 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationDetailResponse {

    private Long id;
    private Long portfolioId;
    private String portfolioName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialAmount;
    private BigDecimal finalAmount;
    private RebalancingCycle rebalancingCycle;
    private Summary summary;
    private List<MonthlyReturnItem> monthlyReturns;
    private List<EtfPerformanceItem> etfPerformance;
    private BenchmarkComparison benchmarkComparison;
    private LocalDateTime createdAt;

    /**
     * 성과 요약
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private BigDecimal totalReturn;
        private BigDecimal totalReturnRate;
        private BigDecimal annualizedReturn;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private BigDecimal volatility;
    }

    /**
     * 월별 수익률
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyReturnItem {
        private String month;
        private BigDecimal value;
        private BigDecimal returnRate;

        public static MonthlyReturnItem from(SimulationMonthlyReturn monthlyReturn) {
            return MonthlyReturnItem.builder()
                    .month(monthlyReturn.getMonth())
                    .value(monthlyReturn.getValue())
                    .returnRate(monthlyReturn.getReturnRate())
                    .build();
        }
    }

    /**
     * ETF 성과
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfPerformanceItem {
        private Long etfId;
        private String ticker;
        private String name;
        private BigDecimal weightPct;
        private BigDecimal returnRate;
        private BigDecimal contribution;

        public static EtfPerformanceItem from(SimulationEtfPerformance performance) {
            return EtfPerformanceItem.builder()
                    .etfId(performance.getEtf().getId())
                    .ticker(performance.getEtf().getStockCode())
                    .name(performance.getEtf().getName())
                    .weightPct(performance.getWeightPct())
                    .returnRate(performance.getReturnRate())
                    .contribution(performance.getContribution())
                    .build();
        }
    }

    /**
     * 벤치마크 비교
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkComparison {
        private String benchmark;
        private BigDecimal benchmarkReturn;
        private BigDecimal alpha;
    }

    public static SimulationDetailResponse from(Simulation simulation,
                                                  List<SimulationMonthlyReturn> monthlyReturns,
                                                  List<SimulationEtfPerformance> etfPerformances) {
        return SimulationDetailResponse.builder()
                .id(simulation.getId())
                .portfolioId(simulation.getPortfolio().getId())
                .portfolioName(simulation.getPortfolio().getName())
                .startDate(simulation.getStartDate())
                .endDate(simulation.getEndDate())
                .initialAmount(simulation.getInitialAmount())
                .finalAmount(simulation.getFinalAmount())
                .rebalancingCycle(simulation.getRebalancingCycle())
                .summary(Summary.builder()
                        .totalReturn(simulation.getTotalReturn())
                        .totalReturnRate(simulation.getTotalReturnRate())
                        .annualizedReturn(simulation.getAnnualizedReturn())
                        .maxDrawdown(simulation.getMaxDrawdown())
                        .sharpeRatio(simulation.getSharpeRatio())
                        .volatility(simulation.getVolatility())
                        .build())
                .monthlyReturns(monthlyReturns.stream()
                        .map(MonthlyReturnItem::from)
                        .toList())
                .etfPerformance(etfPerformances.stream()
                        .map(EtfPerformanceItem::from)
                        .toList())
                .benchmarkComparison(BenchmarkComparison.builder()
                        .benchmark("KOSPI 200")
                        .benchmarkReturn(BigDecimal.ZERO)
                        .alpha(simulation.getTotalReturnRate())
                        .build())
                .createdAt(simulation.getCreatedAt())
                .build();
    }
}
