package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfDividend;
import com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtfDividendJpaRepository extends JpaRepository<EtfDividend, Long> {

    @Query("""
        SELECT new com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData(
            d.paymentDate,
            d.amountPerUnit
        )
        FROM EtfDividend d
        JOIN d.etf e
        WHERE e.stockCode = :ticker
        ORDER BY d.paymentDate DESC
    """)
    List<EtfDividendsData> findDividendsByTicker(@Param("ticker") String ticker);
}
