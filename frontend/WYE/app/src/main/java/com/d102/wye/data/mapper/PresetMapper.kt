package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.PresetDetailResponse
import com.d102.wye.data.remote.dto.response.PresetListItemResponse
import com.d102.wye.domain.model.BundleEtfItem
import com.d102.wye.domain.model.BundleType
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail

fun PresetListItemResponse.toDomain() = EtfBundle(
    id = presetId,
    name = title,
    summary = description,
    bundleType = BundleType.from(imageTag),
    tags = presetTagList
)

fun PresetDetailResponse.toDomain() = EtfBundleDetail(
    id = presetId,
    name = presetName,
    description = description,
    bundleType = BundleType.from(imageTag),
    etfItems = presetResponseList.map { BundleEtfItem(ticker = it.ticker, name = it.name) }
)

