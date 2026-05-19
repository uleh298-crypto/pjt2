package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PresetPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresetPortfolioJpaRepository extends JpaRepository<PresetPortfolio, Long> {

    List<PresetPortfolio> findAllByOrderByDisplayOrderAsc();
}
