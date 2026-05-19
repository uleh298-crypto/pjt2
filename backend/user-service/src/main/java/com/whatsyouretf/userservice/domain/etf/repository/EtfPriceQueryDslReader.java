package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EtfPriceQueryDslReader {
    Page<EtfPrice> findByTickerAndDateRange(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
