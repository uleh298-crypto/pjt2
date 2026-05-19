package com.whatsyouretf.userservice.domain.portfolio.repository;

import java.util.List;
import java.util.Map;

public interface PortfolioQuerydslRepository {
        List<PortfolioEtfInfo> getPortfolioEtfs(Long portfolioId);

        Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioList);
}
