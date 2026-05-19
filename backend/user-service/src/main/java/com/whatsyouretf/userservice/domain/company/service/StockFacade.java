package com.whatsyouretf.userservice.domain.company.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockFacade {
        private final EtfService etfService;

        public List<EtfIncludesStock> getIncludingStock(String ticker) {
                List<EtfStockComposition> etfsIncludingStock = etfService.getEtfsIncludingStock(ticker);

                return etfsIncludingStock.stream()
                        .map(etfStockComposition -> {
                                Etf etf = etfStockComposition.getEtf();
                                return EtfIncludesStock.of(
                                        etf.getName(),
                                        etf.getAssetManager(),
                                        etf.getStockCode(),
                                        etfStockComposition.getWeightPct()
                                );
                        }).toList();

        }
}
