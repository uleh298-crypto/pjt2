package com.whatsyouretf.userservice.domain.user.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.user.entity.UserHoldingEtf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 보유 ETF 응답 DTO (마이데이터 연동)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingEtfResponse {

    /** ETF ID */
    private Long etfId;

    /** 종목코드 */
    private String stockCode;

    /** ETF 명칭 */
    private String name;

    /** 카테고리 */
    private String category;

    /** 자산운용사 */
    private String assetManager;

    /** 보유 수량 */
    private Integer quantity;

    /** 평균 매입가 */
    private BigDecimal avgPrice;

    /** 현재가 */
    private BigDecimal currentPrice;

    /** 평가금액 (수량 × 현재가) */
    private BigDecimal evaluationAmount;

    /** 손익금액 (평가금액 - 매입금액) */
    private BigDecimal profitLossAmount;

    /** 손익률 (%) */
    private BigDecimal profitLossRate;

    /** 마이데이터 동기화 시점 */
    private LocalDateTime syncedAt;

    /**
     * Entity -> DTO 변환 (시세 정보 없음)
     */
    public static HoldingEtfResponse from(UserHoldingEtf holding) {
        Etf etf = holding.getEtf();
        return HoldingEtfResponse.builder()
                .etfId(etf.getId())
                .stockCode(etf.getStockCode())
                .name(etf.getName())
                .assetManager(etf.getAssetManager())
                .quantity(holding.getQuantity())
                .avgPrice(holding.getAvgPrice())
                .syncedAt(holding.getSyncedAt())
                .build();
    }

    /**
     * Entity + Price -> DTO 변환 (수익률 계산 포함)
     */
    public static HoldingEtfResponse from(UserHoldingEtf holding, EtfPrice latestPrice) {
        Etf etf = holding.getEtf();
        BigDecimal currentPrice = latestPrice != null ? latestPrice.getClose() : null;

        // 평가금액, 손익 계산
        BigDecimal evaluationAmount = null;
        BigDecimal profitLossAmount = null;
        BigDecimal profitLossRate = null;

        if (currentPrice != null && holding.getQuantity() != null) {
            evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));

            if (holding.getAvgPrice() != null && holding.getAvgPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal purchaseAmount = holding.getAvgPrice()
                        .multiply(BigDecimal.valueOf(holding.getQuantity()));
                profitLossAmount = evaluationAmount.subtract(purchaseAmount);
                profitLossRate = profitLossAmount
                        .divide(purchaseAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        return HoldingEtfResponse.builder()
                .etfId(etf.getId())
                .stockCode(etf.getStockCode())
                .name(etf.getName())
                .assetManager(etf.getAssetManager())
                .quantity(holding.getQuantity())
                .avgPrice(holding.getAvgPrice())
                .currentPrice(currentPrice)
                .evaluationAmount(evaluationAmount)
                .profitLossAmount(profitLossAmount)
                .profitLossRate(profitLossRate)
                .syncedAt(holding.getSyncedAt())
                .build();
    }
}
