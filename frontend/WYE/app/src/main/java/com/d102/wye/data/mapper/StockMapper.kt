package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.RelatedStockResponse
import com.d102.wye.data.remote.dto.response.StockDetailResponse
import com.d102.wye.data.remote.dto.response.StockEtfResponse
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock
import com.d102.wye.domain.model.StockEtf

fun StockDetailResponse.toDomain(etfs: List<StockEtf>) = Stock(
    ticker = ticker,
    name = stockName,
    currentPrice = currentPrice,
    changeAmount = dailyFluctuation.toLong(),
    marketCap = marketCapitalization,
    description = description,
    containedEtfs = etfs,
)

fun StockEtfResponse.toDomain() = StockEtf(
    ticker = ticker,
    name = etfName,
    manager = manager,
    weight = ratio,
)

fun RelatedStockResponse.toDomain() = RelatedStock(
    ticker = ticker,
    name = companyName,
    description = "$relationType ($industryName)",
)
