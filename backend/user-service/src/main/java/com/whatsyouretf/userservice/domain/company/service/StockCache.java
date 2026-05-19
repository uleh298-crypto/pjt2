package com.whatsyouretf.userservice.domain.company.service;

import com.whatsyouretf.userservice.domain.company.repository.StockInfo;

import java.util.Map;
import java.util.Set;

public interface StockCache {
    StockInfo get(String ticker, String description);

    /**
     * 여러 종목의 가격 정보를 배치로 조회
     */
    Map<String, StockInfo> getAll(Set<String> tickers);
}
