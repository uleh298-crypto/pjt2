package com.whatsyouretf.userservice.domain.portfolio.repository;

import java.util.List;

public interface PortfolioIssueRepository {
    List<PortfolioIssues> getIssuesByPortfolioId(Long portfolioId);
}
