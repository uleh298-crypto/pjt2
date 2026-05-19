package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EtfReader {
    Etf read(String ticker);

    EtfCurrentInfo getInfo(String ticker);

    Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable);

    List<EtfSummary> readAllEtfList(EtfQuery query);

    Map<String, Etf> getValidEtfs(List<String> list);

    Map<String, EtfCurrentInfo> getInfosMap(Set<String> tickers);

    List<EtfCurrentInfo> getTopTenEtfs();
}
