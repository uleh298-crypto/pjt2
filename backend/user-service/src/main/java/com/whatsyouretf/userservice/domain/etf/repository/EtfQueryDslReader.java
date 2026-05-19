package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EtfQueryDslReader {
    Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable);

    /** 캐시 기반 정렬(dailyReturn/volume)을 위해 페이징 없이 전체 조회 */
    List<EtfSummary> readAllEtfList(EtfQuery query);
}
