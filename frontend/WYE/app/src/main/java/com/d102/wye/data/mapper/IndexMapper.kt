package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.IndexItemResponse
import com.d102.wye.data.remote.dto.response.IndexPageResponse
import com.d102.wye.domain.model.IndexPoint

fun IndexItemResponse.toDomain() = IndexPoint(
    date       = tradingDate,
    close      = close,
    marketType = marketType,
)

fun IndexPageResponse.toDomain(): List<IndexPoint> = content.map { it.toDomain() }
