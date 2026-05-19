package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfMarketData
import com.d102.wye.domain.model.EtfFilter
import com.d102.wye.domain.model.EtfLikeData
import com.d102.wye.domain.model.EtfPage
import com.d102.wye.domain.model.EtfPriceData
import com.d102.wye.domain.model.TopVolumeEtf
import com.d102.wye.domain.model.TopVolumeEtfSnapshot
import kotlinx.coroutines.flow.Flow

interface EtfRepository {
    suspend fun getTopVolumeEtfs(): BaseResult<TopVolumeEtfSnapshot>
    suspend fun getEtfList(filter: EtfFilter = EtfFilter(), page: Int = 0): BaseResult<EtfPage>
    fun getLikedEtfList(): Flow<List<EtfLikeData>>
    suspend fun toggleLike(data: EtfLikeData): BaseResult<Boolean>
    suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail>
    suspend fun getEtfCluster(ticker: String): BaseResult<EtfClusterData>
    suspend fun getEtfPriceHistory(ticker: String, startDate: String, endDate: String, size: Int = 300): BaseResult<List<EtfPriceData>>
    suspend fun getMarketData(ticker: String): BaseResult<EtfMarketData>
}
