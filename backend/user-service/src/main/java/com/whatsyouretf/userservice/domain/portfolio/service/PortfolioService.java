package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioType;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfInfo;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssues;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PortfolioService {
        void savePortfolioEtfs(Map<String, Etf> etfs, List<PortfolioCommand> list, Portfolio portfolio);

        Portfolio savePortfolio(Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod, PortfolioType portfolioType);

        List<Portfolio> getPortfolioList(Long userId);

        Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioIds);

        List<PortfolioEtf> getPortfolio(Long userId, Long portfolioId);

        void updatePortfolio(Long portfolioId, String name);

        void deletePortfolio(Long portfolioId);

        List<PortfolioIssues> getPortfolioIssues(Long portfolioId);
}
