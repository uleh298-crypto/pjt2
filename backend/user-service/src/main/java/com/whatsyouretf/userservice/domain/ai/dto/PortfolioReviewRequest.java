package com.whatsyouretf.userservice.domain.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 포트폴리오 AI 리뷰 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReviewRequest {

    /** 포트폴리오 구성 정보 */
    @NotNull(message = "포트폴리오 정보는 필수입니다.")
    @Valid
    private PortfolioInfo portfolio;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioInfo {

        /** 총 투자금액 (최소 10,000원, 최대 100억원) */
        @Min(value = 10000, message = "최소 투자금액은 10,000원입니다.")
        @Max(value = 10000000000L, message = "최대 투자금액은 100억원입니다.")
        private long totalAmount;

        /** 투자 유형: LUMP_SUM (일시불) / REGULAR_SAVING (적립식) */
        @NotBlank(message = "투자 유형은 필수입니다.")
        @Pattern(regexp = "^(LUMP_SUM|REGULAR_SAVING)$", message = "투자 유형은 LUMP_SUM 또는 REGULAR_SAVING이어야 합니다.")
        private String investmentType;

        /** ETF 목록 (최소 1개, 최대 20개) */
        @NotEmpty(message = "최소 1개의 ETF가 필요합니다.")
        @Size(max = 20, message = "최대 20개의 ETF만 포함할 수 있습니다.")
        @Valid
        private List<EtfInfo> etfs;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfInfo {

        /** ETF 종목 코드 (6자리 영숫자) */
        @NotBlank(message = "ETF 종목 코드는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9]{6}$", message = "ETF 종목 코드는 6자리여야 합니다.")
        private String ticker;

        /** ETF 이름 (최대 200자) */
        @NotBlank(message = "ETF 이름은 필수입니다.")
        @Size(max = 200, message = "ETF 이름은 최대 200자입니다.")
        private String name;

        /** 비중 (%, 1~100) */
        @Min(value = 1, message = "비중은 최소 1%입니다.")
        @Max(value = 100, message = "비중은 최대 100%입니다.")
        private int weight;
    }
}
