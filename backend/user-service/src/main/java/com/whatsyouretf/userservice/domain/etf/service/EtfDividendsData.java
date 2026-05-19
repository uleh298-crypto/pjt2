package com.whatsyouretf.userservice.domain.etf.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EtfDividendsData(
    LocalDate date,
    BigDecimal amount
) {
}
