package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioReader;
import com.whatsyouretf.userservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortfolioReaderImpl implements PortfolioReader {
        private final PortfolioRepository portfolioRepository;
        private final PortfolioQuerydslRepository portfolioQuerydslRepository;
        private final PortfolioIssueRepository portfolioIssueRepository;

        @Override
        public List<Portfolio> getUsersPortfolios(Long userId) {
                return portfolioRepository.findByUserOrderByCreatedAtDesc(User.of(userId));
        }

        @Override
        public Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioList) {
                return portfolioQuerydslRepository.getPortfolioInfoMap(portfolioList);
        }

        @Override
        public List<PortfolioEtf> getPortfolioDetail(Long portfolioId) {
                return portfolioRepository.findByPortfolioId(portfolioId);
        }

        @Override
        public Portfolio getPortfolio(Long portfolioId) {
                return portfolioRepository.findById(portfolioId).orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        }

        @Override
        public List<PortfolioIssues> getPortfolioIssues(Long portfolioId) {
                return portfolioIssueRepository.getIssuesByPortfolioId(portfolioId);
        }
}
