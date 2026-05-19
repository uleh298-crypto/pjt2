package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;

import java.math.BigDecimal;

public record EtfListRequest(
        String riskType,
        String strategy,
        String sector,
        BigDecimal dividendYield,
        String dividendFrequency,
        Boolean isDerivatives,
        Boolean isLeverage,
        Boolean isInverse,
        BigDecimal perLow,
        BigDecimal perHigh,
        BigDecimal pbrLow,
        BigDecimal pbrHigh,
        BigDecimal roeLow,
        BigDecimal roeHigh,
        BigDecimal commission,
        BigDecimal aum,
        String sortedBy,
        String searchName,
        Boolean isFavorite
) {
        public EtfQuery toQuery(Long userId) {
                return new EtfQuery(
                        riskType,
                        strategy,
                        sector,
                        dividendYield,
                        dividendFrequency,
                        isDerivatives,
                        isLeverage,
                        isInverse,
                        perLow,
                        perHigh,
                        pbrLow,
                        pbrHigh,
                        roeLow,
                        roeHigh,
                        commission,
                        aum,
                        sortedBy,
                        searchName,
                        userId,
                        isFavorite
                );
        }
}
