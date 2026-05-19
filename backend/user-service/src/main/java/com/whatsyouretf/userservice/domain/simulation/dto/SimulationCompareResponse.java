package com.whatsyouretf.userservice.domain.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 포트폴리오 비교 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationCompareResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialAmount;
    private List<PortfolioComparison> comparisons;
    private Benchmark benchmark;

    /**
     * 포트폴리오별 비교 결과
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioComparison {
        private Long portfolioId;
        private String portfolioName;
        private BigDecimal finalAmount;
        private BigDecimal totalReturnRate;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private int rank;
    }

    /**
     * 벤치마크 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Benchmark {
        private String name;
        private BigDecimal returnRate;
    }
}
