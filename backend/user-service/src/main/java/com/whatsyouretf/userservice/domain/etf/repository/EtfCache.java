package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;

import java.util.List;

public interface EtfCache {
    EtfCurrentInfo findByTicker(String ticker);

    List<EtfCurrentInfo> getTopTenEtfsAndSortedByVolume();
}
