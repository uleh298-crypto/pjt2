package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioType;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfInfo;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioIssues;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

        private final PortfolioStore portfolioStore;

        private final PortfolioReader portfolioReader;

        @Override
        public Portfolio savePortfolio(Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod, PortfolioType portfolioType) {
                return portfolioStore.storePortfolio(Portfolio.createPortfolio(userId, portfolioName, investAmount, investPeriod, portfolioType));
        }

        @Override
        public List<Portfolio> getPortfolioList(Long userId) {
                return portfolioReader.getUsersPortfolios(userId);
        }

        @Override
        public Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioIds) {
                return portfolioReader.getPortfolioInfoMap(portfolioIds);
        }

        @Override
        public List<PortfolioEtf> getPortfolio(Long userId, Long portfolioId) {
                return portfolioReader.getPortfolioDetail(portfolioId);
        }

        @Override
        @Transactional
        public void updatePortfolio(Long portfolioId, String name) {
                Portfolio portfolio = portfolioReader.getPortfolio(portfolioId);
                portfolio.update(name);
        }

        @Override
        public void deletePortfolio(Long portfolioId) {
                portfolioStore.deletePortfolio(portfolioId);
        }

        @Override
        public List<PortfolioIssues> getPortfolioIssues(Long portfolioId) {
                return portfolioReader.getPortfolioIssues(portfolioId);
        }

        @Override
        public void savePortfolioEtfs(Map<String, Etf> etfs, List<PortfolioCommand> list, Portfolio portfolio) {
                portfolioStore.storePortfolioEtfs(portfolio, etfs, list);
        }
}
