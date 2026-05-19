package com.whatsyouretf.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 보유 ETF 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingEtfListResponse {

    /** 보유 ETF 목록 */
    private List<HoldingEtfResponse> holdings;

    /** 총 개수 */
    private int totalCount;

    /** 총 평가금액 */
    private BigDecimal totalEvaluationAmount;

    /** 총 손익금액 */
    private BigDecimal totalProfitLossAmount;

    /** 총 손익률 (%) */
    private BigDecimal totalProfitLossRate;

    /** 마지막 동기화 시점 */
    private LocalDateTime lastSyncedAt;

    public static HoldingEtfListResponse of(List<HoldingEtfResponse> holdings, LocalDateTime lastSyncedAt) {
        BigDecimal totalEval = BigDecimal.ZERO;
        BigDecimal totalPL = BigDecimal.ZERO;
        BigDecimal totalPurchase = BigDecimal.ZERO;

        for (HoldingEtfResponse h : holdings) {
            if (h.getEvaluationAmount() != null) {
                totalEval = totalEval.add(h.getEvaluationAmount());
            }
            if (h.getProfitLossAmount() != null) {
                totalPL = totalPL.add(h.getProfitLossAmount());
            }
            if (h.getAvgPrice() != null && h.getQuantity() != null) {
                totalPurchase = totalPurchase.add(
                        h.getAvgPrice().multiply(BigDecimal.valueOf(h.getQuantity()))
                );
            }
        }

        BigDecimal totalPLRate = null;
        if (totalPurchase.compareTo(BigDecimal.ZERO) > 0) {
            totalPLRate = totalPL
                    .divide(totalPurchase, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return HoldingEtfListResponse.builder()
                .holdings(holdings)
                .totalCount(holdings.size())
                .totalEvaluationAmount(totalEval)
                .totalProfitLossAmount(totalPL)
                .totalProfitLossRate(totalPLRate)
                .lastSyncedAt(lastSyncedAt)
                .build();
    }
}
