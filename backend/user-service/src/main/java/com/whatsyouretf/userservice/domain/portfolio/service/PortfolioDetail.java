package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.portfolio.controller.PortfolioEtfCount;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public record PortfolioDetail(
        Long portfolioId,
        String portfolioName,
        List<PortfolioEtfCount> counts,
        BigDecimal investAmount,
        LocalDateTime createdAt,
        PortfolioType portfolioType
) {
        public static PortfolioDetail of(
                List<PortfolioEtf> etfs
        ) {
                Portfolio portfolio;
                try {
                        portfolio = etfs.getFirst().getPortfolio();
                } catch (NoSuchElementException e) {
                        throw new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND);
                }

                return new PortfolioDetail(
                        portfolio.getId(),
                        portfolio.getName(),
                        etfs.stream().map(etf -> new PortfolioEtfCount(etf.getEtf().getStockCode(), etf.getEtfCount(), etf.getEtf().getName())).toList(),
                        portfolio.getInvestAmount(),
                        portfolio.getCreatedAt(),
                        portfolio.getPortfolioType()
                );
        }
}
