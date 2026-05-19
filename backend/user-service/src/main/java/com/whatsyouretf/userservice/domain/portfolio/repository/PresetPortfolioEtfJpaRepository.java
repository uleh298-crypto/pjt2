package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PresetPortfolioEtf;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresetPortfolioEtfJpaRepository extends JpaRepository<PresetPortfolioEtf, Long> {

    @EntityGraph(attributePaths = {"etf"})
    List<PresetPortfolioEtf> findAllByPresetPortfolioId(Long presetPortfolioId);
}
