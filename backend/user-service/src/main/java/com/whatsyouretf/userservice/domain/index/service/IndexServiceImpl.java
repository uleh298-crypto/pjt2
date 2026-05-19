package com.whatsyouretf.userservice.domain.index.service;

import com.whatsyouretf.userservice.domain.index.entity.MarketType;
import com.whatsyouretf.userservice.domain.index.repository.BenchmarkIndexPriceRepository;
import com.whatsyouretf.userservice.domain.index.repository.IndexSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
        private final BenchmarkIndexPriceRepository benchmarkIndexPriceRepository;
        @Override
        public Page<IndexSummary> getIndexHistory(MarketType marketType, Pageable pageable) {
                return benchmarkIndexPriceRepository.findAllByMarketType(marketType, pageable);
        }
}
