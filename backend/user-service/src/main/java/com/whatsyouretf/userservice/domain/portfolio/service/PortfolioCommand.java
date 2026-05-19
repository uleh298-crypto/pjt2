package com.whatsyouretf.userservice.domain.portfolio.service;

import java.math.BigDecimal;

public record PortfolioCommand(
        String stockCode,
        BigDecimal counts
) {
}
