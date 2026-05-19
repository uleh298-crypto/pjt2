package com.whatsyouretf.userservice.domain.simulation.dto;

import com.whatsyouretf.userservice.domain.simulation.entity.Simulation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 시뮬레이션 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationListResponse {

    private List<SimulationSummary> simulations;
    private int page;
    private int totalPages;
    private long totalElements;

    /**
     * 시뮬레이션 요약 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationSummary {

        private Long id;
        private Long portfolioId;
        private String portfolioName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal initialAmount;
        private BigDecimal finalAmount;
        private BigDecimal totalReturn;
        private LocalDateTime createdAt;

        public static SimulationSummary from(Simulation simulation) {
            return SimulationSummary.builder()
                    .id(simulation.getId())
                    .portfolioId(simulation.getPortfolio().getId())
                    .portfolioName(simulation.getPortfolio().getName())
                    .startDate(simulation.getStartDate())
                    .endDate(simulation.getEndDate())
                    .initialAmount(simulation.getInitialAmount())
                    .finalAmount(simulation.getFinalAmount())
                    .totalReturn(simulation.getTotalReturnRate())
                    .createdAt(simulation.getCreatedAt())
                    .build();
        }
    }
}
