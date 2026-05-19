package com.whatsyouretf.userservice.domain.simulation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 포트폴리오 비교 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationCompareRequest {

    @NotNull(message = "포트폴리오 ID 목록은 필수입니다")
    @Size(min = 2, max = 5, message = "포트폴리오는 2~5개를 선택해야 합니다")
    private List<@Positive(message = "포트폴리오 ID는 양수여야 합니다") Long> portfolioIds;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    @NotNull(message = "초기 투자금액은 필수입니다")
    @DecimalMin(value = "10000", message = "초기 투자금액은 최소 10,000원입니다")
    @DecimalMax(value = "10000000000", message = "초기 투자금액은 최대 100억원입니다")
    private BigDecimal initialAmount;
}
