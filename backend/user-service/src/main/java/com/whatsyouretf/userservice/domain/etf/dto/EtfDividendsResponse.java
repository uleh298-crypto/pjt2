package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.service.EtfDividendsData;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EtfDividendsResponse(
        LocalDate date,
        BigDecimal amount
) {
    public static EtfDividendsResponse of(EtfDividendsData data) {
        return new EtfDividendsResponse(
            data.date(),
            data.amount()
        );
    }
}
