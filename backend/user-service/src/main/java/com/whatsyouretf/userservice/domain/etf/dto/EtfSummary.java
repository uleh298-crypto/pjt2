package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;

public record EtfSummary(
        Long etfId,
        String ticker,
        String etfName,
        Boolean isFavorite,
        String riskType,
        BigDecimal nav
) {
}
