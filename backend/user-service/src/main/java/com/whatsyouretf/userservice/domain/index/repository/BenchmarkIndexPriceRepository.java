package com.whatsyouretf.userservice.domain.index.repository;

import com.whatsyouretf.userservice.domain.index.entity.BenchmarkIndexPrice;
import com.whatsyouretf.userservice.domain.index.entity.MarketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BenchmarkIndexPriceRepository extends JpaRepository<BenchmarkIndexPrice, Long> {
        @Query("""
            SELECT new com.whatsyouretf.userservice.domain.index.repository.IndexSummary(
                    i.close,
                    i.marketType,
                    i.tradingDate
            )
            FROM BenchmarkIndexPrice i
            WHERE i.marketType = :marketType
        """)
        Page<IndexSummary> findAllByMarketType(@Param("marketType") MarketType marketType, Pageable pageable);
}
