package com.whatsyouretf.userservice.domain.user.service.impl;

import java.math.BigDecimal;

public record MyDataEtfCount(
        String ticker,
        BigDecimal counts
) {
}
