package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.EtfClusterDataResponse
import com.d102.wye.data.remote.dto.response.EtfDetailResponse
import com.d102.wye.data.remote.dto.response.EtfMarketDataResponse
import com.d102.wye.data.remote.dto.response.EtfListItemResponse
import com.d102.wye.data.remote.dto.response.EtfPriceHistoryPageResponse
import com.d102.wye.data.remote.dto.response.EtfPricePointResponse
import com.d102.wye.data.remote.dto.response.EtfSectorResponse
import com.d102.wye.data.remote.dto.response.TopVolumeEtfResponse
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfCluster
import com.d102.wye.domain.model.EtfMarketData
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfClusterStock
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfPriceData
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.domain.model.TopVolumeEtf

// POST /api/v1/etfs 리스트 항목 → Etf (API가 주는 7개 필드만)
fun EtfListItemResponse.toDomain() = Etf(
    etfId = etfId,
    ticker = ticker,
    name = etfName,
    currentPrice = etfPrice,
    changeRate = dailyReturn,
    changeAmount = dailyFluctuation,
    riskType = riskType,
    isFavorite = isFavorite,
)

// GET /api/v1/etfs/{ticker} → EtfDetail
fun EtfDetailResponse.toDomain() = EtfDetail(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice.toLong(),
    dailyFluctuation = dailyFluctuation.toLong(),
    dailyFluctuationRatio = dailyFluctuationRatio,
    volume = volume.toLong(),
    company = company,
    riskGrade = riskGrade,
    riskType = riskType,
    expenseRatio = expenseRatio,
    per = per,
    pbr = pbr,
    roe = roe,
    aum = aum.toLong(),
    listingDate = listingDate,
    inav = inav.toLong(),
    inavChangeAmount = inavChangeAmount.toLong(),
    inavChangeRate = inavChangeRate,
)

// GET /api/v1/etfs/{ticker}/market-data → EtfMarketData
fun EtfMarketDataResponse.toDomain() = EtfMarketData(
    ticker = ticker,
    currentPrice = currentPrice,
    dailyReturn = dailyReturn,
    volume = volume,
)

// GET /api/v1/etfs/{ticker}/clusters → EtfClusterData
fun EtfClusterDataResponse.toDomain() = EtfClusterData(
    englishName = englishName ?: "",
    sectors = sectors.map { it.toDomain() },
    influentialStocks = influentialStocks.map {
        InfluentialStock(
            ticker = it.ticker,
            name = it.name,
            weight = it.weight,
            currentPrice = it.currentPrice,
            changeRate = it.changeRate,
        )
    },
)

fun EtfSectorResponse.toDomain() = EtfCluster(
    name = name,
    percentage = percentage,
    stocks = stocks.map { EtfClusterStock(ticker = it.ticker, name = it.name, percentage = it.percentage) },
    aiAnalysis = aiAnalysis,
    assetType = assetType,
)

// GET /api/v1/etfs/{ticker}/price-history 항목 → EtfPricePoint
fun EtfPricePointResponse.toDomain() = EtfPriceData(
    date = date,
    stockPrice = stockPrice,
    dailyReturn = dailyReturn,
    nav = nav,
)

// price-history 페이지 → List<EtfPricePoint>
fun EtfPriceHistoryPageResponse.toDomain() = content.map { it.toDomain() }

// GET /api/v1/etfs 거래량 TOP 10 항목 → TopVolumeEtf
fun TopVolumeEtfResponse.toDomain() = TopVolumeEtf(
    ticker = ticker,
    name = name,
    dailyReturn = dailyReturn,
    volume = volume
)
