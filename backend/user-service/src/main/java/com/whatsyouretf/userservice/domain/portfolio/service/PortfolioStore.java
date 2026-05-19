package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;

import java.util.List;
import java.util.Map;

public interface PortfolioStore {
        void storePortfolioEtfs(Portfolio portfolio, Map<String, Etf> etfs, List<PortfolioCommand> list);

        Portfolio storePortfolio(Portfolio portfolio);

        void deletePortfolio(Long portfolioId);
}
