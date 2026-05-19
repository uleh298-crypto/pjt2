package com.whatsyouretf.userservice.domain.company.service;

import com.whatsyouretf.userservice.domain.company.dto.RelatedStockResponse;
import com.whatsyouretf.userservice.domain.company.repository.StockInfo;

import java.util.List;

/**
 * 주식 서비스 인터페이스
 */
public interface StockService {

    /**
     * 종목 태그 조회
     *
     * @param ticker 종목 티커
     * @return 태그 목록 [시장유형, 산업그룹명, 산업분류명]
     */
    List<String> getStockTags(String ticker);

    /**
     * 관련 종목 조회 (같은 산업분류, 3개 고정)
     *
     * @param ticker 기준 종목 티커
     * @return 관련 종목 목록
     */
    List<RelatedStockResponse> getRelatedStocks(String ticker);

    StockInfo getStockInfo(String ticker);
}
