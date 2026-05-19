package com.whatsyouretf.userservice.domain.simulation.dto;

import com.whatsyouretf.userservice.domain.simulation.entity.RebalancingCycle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 시뮬레이션 저장 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSaveRequest {

    @NotNull(message = "포트폴리오 ID는 필수입니다")
    @Positive(message = "포트폴리오 ID는 양수여야 합니다")
    private Long portfolioId;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    @NotNull(message = "초기 투자금액은 필수입니다")
    @DecimalMin(value = "10000", message = "초기 투자금액은 최소 10,000원입니다")
    @DecimalMax(value = "10000000000", message = "초기 투자금액은 최대 100억원입니다")
    private BigDecimal initialAmount;

    private RebalancingCycle rebalancingCycle;

    @NotNull(message = "시뮬레이션 결과는 필수입니다")
    @Valid
    private SimulationResult result;

    /**
     * 시뮬레이션 결과 객체
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationResult {

        @NotNull(message = "최종 금액은 필수입니다")
        private BigDecimal finalAmount;

        @NotNull(message = "총 수익금액은 필수입니다")
        private BigDecimal totalReturn;

        @NotNull(message = "총 수익률은 필수입니다")
        private BigDecimal totalReturnRate;

        @NotNull(message = "연환산 수익률은 필수입니다")
        private BigDecimal annualizedReturn;

        @NotNull(message = "최대 낙폭은 필수입니다")
        private BigDecimal maxDrawdown;

        @NotNull(message = "샤프 비율은 필수입니다")
        private BigDecimal sharpeRatio;

        @NotNull(message = "변동성은 필수입니다")
        private BigDecimal volatility;

        @NotNull(message = "월별 수익률은 필수입니다")
        @Valid
        private List<MonthlyReturn> monthlyReturns;

        @NotNull(message = "ETF 성과는 필수입니다")
        @Valid
        private List<EtfPerformance> etfPerformance;
    }

    /**
     * 월별 수익률
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyReturn {

        @NotBlank(message = "월은 필수입니다")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "월 형식은 YYYY-MM이어야 합니다")
        private String month;

        @NotNull(message = "금액은 필수입니다")
        private BigDecimal value;

        private BigDecimal returnRate;
    }

    /**
     * ETF별 성과
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfPerformance {

        @NotNull(message = "ETF ID는 필수입니다")
        @Positive(message = "ETF ID는 양수여야 합니다")
        private Long etfId;

        @NotNull(message = "비중은 필수입니다")
        private BigDecimal weightPct;

        private BigDecimal returnRate;

        private BigDecimal contribution;
    }
}
