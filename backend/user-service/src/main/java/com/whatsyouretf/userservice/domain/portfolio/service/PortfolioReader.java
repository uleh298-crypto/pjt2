package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfInfo;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssues;

import java.util.List;
import java.util.Map;

public interface PortfolioReader {
        List<Portfolio> getUsersPortfolios(Long userId);

        Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> b);

        List<PortfolioEtf> getPortfolioDetail(Long portfolioId);

        Portfolio getPortfolio(Long portfolioId);

        List<PortfolioIssues> getPortfolioIssues(Long portfolioId);
}
