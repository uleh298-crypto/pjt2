package com.d102.wye.data.mapper

import com.d102.wye.data.local.entity.LikedEtfEntity
import com.d102.wye.domain.model.EtfLikeData

fun LikedEtfEntity.toDomain() = EtfLikeData(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
)

fun EtfLikeData.toLikedEntity() = LikedEtfEntity(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
)
