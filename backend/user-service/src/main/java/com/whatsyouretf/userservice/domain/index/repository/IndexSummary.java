package com.whatsyouretf.userservice.domain.index.repository;

import com.whatsyouretf.userservice.domain.index.entity.MarketType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexSummary(
        BigDecimal close,
        MarketType marketType,
        LocalDate tradingDate
) {
}
