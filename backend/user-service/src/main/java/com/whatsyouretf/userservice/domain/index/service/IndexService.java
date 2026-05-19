package com.whatsyouretf.userservice.domain.index.service;

import com.whatsyouretf.userservice.domain.index.entity.MarketType;
import com.whatsyouretf.userservice.domain.index.repository.IndexSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IndexService {
        Page<IndexSummary> getIndexHistory(MarketType marketType, Pageable pageable);
}
