package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자의 포트폴리오 전체 삭제
     */
    void deleteAllByUserId(Long userId);

    List<Portfolio> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 마이데이터 포트폴리오 조회
     */
    Optional<Portfolio> findByUserIdAndIsMyDataTrue(Long userId);

    @Query("""
        SELECT pe
        FROM PortfolioEtf pe
        JOIN FETCH pe.etf
        JOIN FETCH pe.portfolio
        WHERE pe.portfolio.id = :portfolioId
    """)
    List<PortfolioEtf> findByPortfolioId(@Param("portfolioId") Long portfolioId);
}
