package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData;

import java.util.List;

public interface EtfDividendRepository {
    List<EtfDividendsData> getDividends(String ticker);
}
