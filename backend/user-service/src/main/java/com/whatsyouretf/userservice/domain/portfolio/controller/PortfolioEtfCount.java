package com.whatsyouretf.userservice.domain.portfolio.controller;

import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioCommand;

import java.math.BigDecimal;

public record PortfolioEtfCount(
        String ticker,
        BigDecimal counts,
        String etfName
) {
        public static PortfolioCommand toQuery(PortfolioEtfCount portfolioEtfCount) {
                return new PortfolioCommand(portfolioEtfCount.ticker(), portfolioEtfCount.counts);
        }
}
