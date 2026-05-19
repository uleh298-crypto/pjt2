package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.EtfOtherComposition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ETF 비주식 구성종목 응답 DTO (선물, 채권, 현금 등)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfOtherCompositionResponse {

    /** 자산 유형 (FUTURES, BOND, CASH, ETF 등) */
    private String assetType;

    /** 자산명 */
    private String assetName;

    /** 비중 (%) */
    private BigDecimal weight;

    public static EtfOtherCompositionResponse from(EtfOtherComposition entity) {
        return EtfOtherCompositionResponse.builder()
                .assetType(entity.getAssetType())
                .assetName(entity.getAssetName())
                .weight(entity.getWeight())
                .build();
    }
}
