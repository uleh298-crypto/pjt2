package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioEtfRepository extends JpaRepository<PortfolioEtf, Long> {
}
