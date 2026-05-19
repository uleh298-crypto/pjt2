package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.etf.repository.EtfIssueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PortfolioIssueRepositoryImpl implements PortfolioIssueRepository {

    private final EtfIssueJpaRepository etfIssueJpaRepository;

    @Override
    public List<PortfolioIssues> getIssuesByPortfolioId(Long portfolioId) {
        return etfIssueJpaRepository.findIssuesByPortfolioId(portfolioId)
                .stream()
                .map(issue -> new PortfolioIssues(
                        issue.getIssueDate(),
                        issue.getTitle(),
                        issue.getDescription()
                ))
                .toList();
    }
}
