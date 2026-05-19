package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioCommand;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortfolioStoreImpl implements PortfolioStore {
        private final PortfolioRepository portfolioRepository;
        private final PortfolioEtfRepository portfolioEtfRepository;

        @Override
        public void storePortfolioEtfs(Portfolio portfolio, Map<String, Etf> etfs, List<PortfolioCommand> list) {
                portfolioEtfRepository.saveAll(
                        list.stream()
                                // TODO : mock 이 아닌 실제로 변경 시 필터 삭제
                                .filter(command -> etfs.containsKey(command.stockCode()))
                                .map(command -> PortfolioEtf.createPortfolioEtf(portfolio, etfs.get(command.stockCode()), command.counts()))
                                .toList()
                );
        }

        @Override
        public Portfolio storePortfolio(Portfolio portfolio) {
                return portfolioRepository.save(portfolio);
        }

        @Override
        public void deletePortfolio(Long portfolioId) {
                portfolioRepository.delete(Portfolio.of(portfolioId));
        }
}
