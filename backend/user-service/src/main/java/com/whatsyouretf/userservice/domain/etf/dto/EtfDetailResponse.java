package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * ETF 상세 응답 DTO (클러스터 뷰 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfDetailResponse {

    /** ETF 티커 */
    private String ticker;

    /** ETF 명칭 */
    private String name;

    /** 현재가 */
    private BigDecimal currentPrice;

    /** 전일 대비 변동금액 */
    private BigDecimal dailyFluctuation;

    /** 전일 대비 변동률 (%) */
    private BigDecimal dailyFluctuationRatio;

    /** iNAV */
    private BigDecimal iNav;

    /** iNAV 변동금액 */
    private BigDecimal iNavChangeAmount;

    /** iNAV 변동률 (%) */
    private BigDecimal iNavChangeRate;

    /** 거래량 */
    private Long volume;

    /** 자산운용사 */
    private String company;

    /** 위험등급 (1~5) */
    private Integer riskGrade;

    /** 위험유형 */
    private String riskType;

    /** 총보수율 (%) */
    private BigDecimal expenseRatio;

    private Double per;

    private Double pbr;

    private Double roe;

    /** 순자산 총액 */
    private Long aum;

    /** 상장일 */
    private LocalDate listingDate;

    /**
     * Entity -> DTO 변환
     */
    public static EtfDetailResponse from(
            Etf etf,
            EtfCurrentInfo info
    ) {
        if (info == null) {
            return new EtfDetailResponse(
                etf.getStockCode(),
                etf.getName(),
                null, null, null, null, null, null, null,
                etf.getAssetManager(),
                etf.getRiskType() != null ? etf.getRiskType().getRiskGrade() : null,
                etf.getRiskType() != null ? etf.getRiskType().getTypeName() : null,
                etf.getExpenseRatio(),
                etf.getFundamental() != null ? etf.getFundamental().getPer() : null,
                etf.getFundamental() != null ? etf.getFundamental().getPbr() : null,
                etf.getFundamental() != null ? etf.getFundamental().getRoe() : null,
                etf.getAum(),
                etf.getListingDate()
            );
        }

        BigDecimal previousPrice = info.previousPrice() == null ? info.currentPrice() : info.previousPrice();
        BigDecimal priceFluctuation = info.currentPrice() != null && info.previousPrice() != null
            ? info.currentPrice().subtract(info.previousPrice()) : BigDecimal.ZERO;
        BigDecimal etfNav = etf.getNav() != null ? etf.getNav() : BigDecimal.ZERO;
        BigDecimal navFluctuation = info.nav() != null ? info.nav().subtract(etfNav) : BigDecimal.ZERO;

        BigDecimal priceFluctuationRatio = (previousPrice != null && previousPrice.compareTo(BigDecimal.ZERO) != 0)
            ? priceFluctuation.divide(previousPrice, 4, RoundingMode.DOWN) : BigDecimal.ZERO;
        BigDecimal navFluctuationRatio = (etfNav.compareTo(BigDecimal.ZERO) != 0)
            ? navFluctuation.divide(etfNav, 4, RoundingMode.DOWN) : BigDecimal.ZERO;

        return new EtfDetailResponse(
            etf.getStockCode(),
            etf.getName(),
            info.currentPrice(),
            priceFluctuation,
            info.dailyReturn(),
            info.nav(),
            navFluctuation,
            navFluctuationRatio,
            info.volume(),
            etf.getAssetManager(),
            etf.getRiskType() != null ? etf.getRiskType().getRiskGrade() : null,
            etf.getRiskType() != null ? etf.getRiskType().getTypeName() : null,
            etf.getExpenseRatio(),
            etf.getFundamental() != null ? etf.getFundamental().getPer() : null,
            etf.getFundamental() != null ? etf.getFundamental().getPbr() : null,
            etf.getFundamental() != null ? etf.getFundamental().getRoe() : null,
            etf.getAum(),
            etf.getListingDate()
        );
    }
}
