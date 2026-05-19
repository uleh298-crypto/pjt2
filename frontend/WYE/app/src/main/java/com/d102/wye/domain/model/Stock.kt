package com.d102.wye.domain.model

data class Stock(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeAmount: Long,
    val marketCap: Long,
    val description: String,
    val containedEtfs: List<StockEtf>,
)

data class StockEtf(
    val ticker: String,
    val name: String,
    val manager: String,
    val weight: Double,
)

data class RelatedStock(
    val ticker: String,
    val name: String,
    val description: String,
)
