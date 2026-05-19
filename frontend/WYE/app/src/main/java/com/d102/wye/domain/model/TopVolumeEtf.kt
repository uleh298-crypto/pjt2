package com.d102.wye.domain.model

data class TopVolumeEtf(
    val ticker: String,
    val name: String,
    val dailyReturn: Double,
    val volume: Long
)
