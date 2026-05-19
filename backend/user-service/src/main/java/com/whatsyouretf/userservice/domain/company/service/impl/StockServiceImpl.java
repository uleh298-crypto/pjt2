package com.whatsyouretf.userservice.domain.company.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.company.dto.RelatedStockResponse;
import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import com.whatsyouretf.userservice.domain.company.entity.Stock;
import com.whatsyouretf.userservice.domain.company.repository.StockInfo;
import com.whatsyouretf.userservice.domain.company.repository.StockRepository;
import com.whatsyouretf.userservice.domain.company.service.StockCache;
import com.whatsyouretf.userservice.domain.company.service.StockService;
import com.whatsyouretf.userservice.domain.etf.entity.IndustryClassification;
import com.whatsyouretf.userservice.domain.etf.repository.IndustryClassificationRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 주식 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final IndustryClassificationRepository industryClassificationRepository;
    private final StockCache stockCache;

    @Value("${app.logo.base-url}")
    private String logoBaseUrl;

    private static final int DEFAULT_RELATED_LIMIT = 3;

    @Override
    public List<String> getStockTags(String ticker) {
        Stock stock = stockRepository.findByTickerWithCompany(ticker)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        List<String> tags = new ArrayList<>();

        // 1. 시장 유형 (KOSPI/KOSDAQ) - stock 테이블에서 조회
        if (stock.getMarketType() != null && !stock.getMarketType().isEmpty()) {
            tags.add(stock.getMarketType());
        }

        // 2. industry_classification에서 그룹명과 세분류명 조회
        CompanyInfo company = stock.getCompany();
        IndustryClassification industry = company.getIndustry();
        if (industry != null) {
            // 그룹명 (대분류): 반도체, 전자/IT, 바이오/의약 등
            if (industry.getGroupName() != null && !industry.getGroupName().isEmpty()) {
                tags.add(industry.getGroupName());
            }
            // 세분류명: LED/조명, 시스템 반도체 등
            if (industry.getName() != null && !industry.getName().isEmpty()) {
                tags.add(industry.getName());
            }
        }

        return tags;
    }

    @Override
    public List<RelatedStockResponse> getRelatedStocks(String ticker) {
        // 기준 종목 조회
        Stock stock = stockRepository.findByTickerWithCompany(ticker)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        IndustryClassification industry = stock.getCompany().getIndustry();
        if (industry == null) {
            return List.of();
        }

        // 같은 산업분류의 관련 종목 조회 (3개 고정)
        List<Stock> relatedStocks = stockRepository.findRelatedStocksByIndustryCode(
                industry.getCode(),
                ticker,
                PageRequest.of(0, DEFAULT_RELATED_LIMIT)
        );

        if (relatedStocks.isEmpty()) {
            return List.of();
        }

        return relatedStocks.stream()
                .map(s -> {
                    IndustryClassification ic = s.getCompany().getIndustry();
                    String industryName = ic != null ? ic.getName() : null;
                    String logoUrl = buildLogoUrl(s.getTicker());
                    return RelatedStockResponse.from(s, industryName, logoUrl);
                })
                .toList();
    }

    @Override
    public StockInfo getStockInfo(String ticker) {
        Stock stock = stockRepository.findByTickerWithCompany(ticker).orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));
        StockInfo cached = stockCache.get(ticker, stock.getDescription());
        if (cached != null) return cached;
        if (stock.getClose() != null) {
            return new StockInfo(ticker, stock.getDescription() != null ? stock.getDescription() : "", stock.getClose(), stock.getClose(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, stock.getDescription());
        }
        return null;
    }

    /**
     * 회사 로고 URL 생성
     */
    private String buildLogoUrl(String ticker) {
        return logoBaseUrl + "/" + ticker + ".png";
    }
}
