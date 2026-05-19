package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtfIssueJpaRepository extends JpaRepository<EtfIssue, Long> {

    @Query("""
        SELECT ei FROM EtfIssue ei
        WHERE ei.etf.id IN (
            SELECT pe.etf.id FROM PortfolioEtf pe WHERE pe.portfolio.id = :portfolioId
        )
        ORDER BY ei.issueDate DESC
        LIMIT 20
    """)
    List<EtfIssue> findIssuesByPortfolioId(@Param("portfolioId") Long portfolioId);
}
