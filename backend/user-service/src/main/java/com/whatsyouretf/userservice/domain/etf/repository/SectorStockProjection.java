package com.whatsyouretf.userservice.domain.etf.repository;

import java.math.BigDecimal;

/**
 * 섹터별 종목 조회용 Projection
 */
public interface SectorStockProjection {
    String getSectorCode();
    String getGroupCode();
    String getStockTicker();
    String getCompanyName();
    BigDecimal getWeightPct();
}
