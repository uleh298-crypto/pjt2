package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ETF JPA 저장소
 */
@Repository
public interface EtfJpaRepository extends JpaRepository<Etf, Long> {
    /**
     * 종목코드로 ETF 조회
     */
    Optional<Etf> findByStockCode(String stockCode);
}
