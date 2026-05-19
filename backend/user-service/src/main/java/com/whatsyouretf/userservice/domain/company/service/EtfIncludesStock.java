package com.whatsyouretf.userservice.domain.company.service;

import java.math.BigDecimal;

public record EtfIncludesStock(
    String etfName,
    String manager,
    String ticker,
    BigDecimal ratio
) {
        public static EtfIncludesStock of(
                String etfName,
                String code,
                String ticker,
                BigDecimal ratio
        ) {
                return new EtfIncludesStock(etfName, AssetManager.getCompanyNameByCode(code), ticker, ratio);
        }
}
