package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PortfolioInfo(
        Long portfolioId,
        String title,
        LocalDateTime createdAt,
        List<PortfolioEtfInfo> etfList,
        BigDecimal totalReturn,
        Boolean isMyData
) {
        public static PortfolioInfo of(Portfolio portfolio, List<PortfolioEtfInfo> portfolioEtfs, BigDecimal totalReturn) {
                return new PortfolioInfo(
                        portfolio.getId(),
                        portfolio.getName(),
                        portfolio.getCreatedAt(),
                        portfolioEtfs,
                        totalReturn,
                        portfolio.getIsMyData()
                );
        }
}
