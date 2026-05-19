package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
@RequiredArgsConstructor
public class EtfDividendRepositoryImpl implements EtfDividendRepository {

    private final EtfDividendJpaRepository jpaRepository;

    @Override
    public List<EtfDividendsData> getDividends(String ticker) {
        return jpaRepository.findDividendsByTicker(ticker);
    }
}
