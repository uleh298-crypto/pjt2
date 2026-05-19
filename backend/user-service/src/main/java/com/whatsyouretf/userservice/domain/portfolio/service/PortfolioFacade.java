package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioType;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfInfo;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PortfolioFacade {
        private final PortfolioService portfolioService;
        private final EtfService etfService;

        @Transactional
        public void savePortfolio(List<PortfolioCommand> commands, Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod, PortfolioType portfolioType) {
                // db 에 있는 etf 목록 조회
                Map<String, Etf> etfs = etfService.getEtfListInTickers(commands.stream().map(PortfolioCommand::stockCode).toList());

                // 포트폴리오 저장
                Portfolio portfolio = portfolioService.savePortfolio(userId, portfolioName, investAmount, investPeriod, portfolioType);

                // 포트폴리오 구성 종목 저장
                portfolioService.savePortfolioEtfs(etfs, commands, portfolio);
        }

        @Transactional(readOnly = true)
        public List<PortfolioInfo> getPortfolioList(Long userId) {
                List<Portfolio> portfolios = portfolioService.getPortfolioList(userId);

                if (portfolios.isEmpty()) {
                        return List.of();
                }

                List<Long> portfolioIds = portfolios.stream()
                        .map(Portfolio::getId)
                        .toList();

                Map<Long, List<PortfolioEtfInfo>> portfolioEtfMap =
                        portfolioService.getPortfolioInfoMap(portfolioIds);

                Set<String> tickers = portfolioEtfMap.values().stream()
                        .flatMap(List::stream)
                        .map(PortfolioEtfInfo::ticker)
                        .collect(Collectors.toSet());

                Map<String, EtfCurrentInfo> currentInfoMap =
                        etfService.getEtfCurrentInfoMap(tickers);

                return portfolios.stream()
                        .map(portfolio -> {
                                List<PortfolioEtfInfo> etfs =
                                        portfolioEtfMap.getOrDefault(portfolio.getId(), List.of());

                                BigDecimal totalPrice = etfs.stream()
                                        .map(etf -> currentInfoMap.getOrDefault(etf.ticker(), EtfCurrentInfo.empty()).currentPrice())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal ratio = portfolio.getInvestAmount().compareTo(BigDecimal.ZERO) == 0
                                        ? BigDecimal.ZERO
                                        : totalPrice.divide(portfolio.getInvestAmount(), 4, RoundingMode.DOWN);

                                return new PortfolioInfo(
                                        portfolio.getId(),
                                        portfolio.getName(),
                                        portfolio.getCreatedAt(),
                                        etfs,
                                        ratio,
                                        portfolio.getIsMyData()
                                );
                        })
                        .toList();
        }
}
