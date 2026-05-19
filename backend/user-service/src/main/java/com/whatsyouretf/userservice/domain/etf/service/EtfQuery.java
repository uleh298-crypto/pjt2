package com.whatsyouretf.userservice.domain.etf.service;

import java.math.BigDecimal;

public record EtfQuery(
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
        Long userId,
        Boolean isFavorite
) {
}
