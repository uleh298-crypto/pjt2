package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.IndexApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.IndexPoint
import com.d102.wye.domain.repository.IndexRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IndexRepositoryImpl @Inject constructor(
    private val indexApiService: IndexApiService,
) : BaseRepository(), IndexRepository {

    override suspend fun getIndex(
        marketType: String,
        startDate:  String,
        endDate:    String,
    ): BaseResult<List<IndexPoint>> {
        return safeApiCall {
            indexApiService.getIndex(marketType = marketType, size = 1000)
        }.map { page ->
            page.toDomain()
                .filter { it.date in startDate..endDate }
                .sortedBy { it.date }
        }
    }
}
