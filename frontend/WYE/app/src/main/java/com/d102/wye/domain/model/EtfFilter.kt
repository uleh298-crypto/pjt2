package com.d102.wye.domain.model

data class EtfFilter(
    val riskType: String? = null,
    val strategy: String? = null,
    val sector: String? = null,
    val dividendYield: Double? = null,
    val dividendFrequency: String? = null,
    val isDerivatives: Boolean? = null,
    val isLeverage: Boolean? = null,
    val isInverse: Boolean? = null,
    val perLow: Double? = null,
    val perHigh: Double? = null,
    val pbrLow: Double? = null,
    val pbrHigh: Double? = null,
    val roeLow: Double? = null,
    val roeHigh: Double? = null,
    val commission: Double? = null,
    val aum: Long? = null,
    val sortedBy: String? = null,
    val searchName: String? = null,
    val isFavorite: Boolean? = null,
)
