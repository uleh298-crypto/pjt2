package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.EtfDividendHistoryResponse
import com.d102.wye.data.remote.dto.response.EtfPriceHistoryResponse
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfMonthlyDividend
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.EtfPricePoint

fun EtfPriceHistoryResponse.toDomain(ticker: String): EtfPriceHistory =
    EtfPriceHistory(
        ticker = ticker,
        content = content.map { dto ->
            EtfPricePoint(
                date = dto.date,
                stockPrice = dto.stockPrice,
                dailyReturn = dto.dailyReturn
            )
        },
        totalElements = totalElements,
        totalPages = totalPages,
        last = last
    )

fun EtfDividendHistoryResponse.toDomain(ticker: String): EtfDividendHistory =
    EtfDividendHistory(
        etfId = etfId,
        etfName = etfName,
        ticker = ticker,
        dividends = dividends.map { dto ->
            EtfMonthlyDividend(
                month = dto.month,
                dividend = dto.dividend
            )
        }
    )