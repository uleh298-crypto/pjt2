package com.whatsyouretf.userservice.domain.etf.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EtfPriceInfo (
    LocalDate date,
    BigDecimal nav,
    BigDecimal close,
    Long volume,
    BigDecimal dailyReturn
){
}
