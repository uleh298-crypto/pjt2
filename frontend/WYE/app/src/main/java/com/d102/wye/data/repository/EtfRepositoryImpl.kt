package com.d102.wye.data.repository

import com.d102.wye.data.local.dao.LikedEtfDao
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.mapper.toLikedEntity
import com.d102.wye.data.remote.api.EtfApiService
import com.d102.wye.data.remote.dto.request.EtfListRequest
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfMarketData
import com.d102.wye.domain.model.EtfFilter
import com.d102.wye.domain.model.EtfLikeData
import com.d102.wye.domain.model.EtfPage
import com.d102.wye.domain.model.EtfPriceData
import com.d102.wye.domain.model.TopVolumeEtfSnapshot
import com.d102.wye.domain.repository.EtfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EtfRepositoryImpl @Inject constructor(
    private val etfApiService: EtfApiService,
    private val likedEtfDao: LikedEtfDao,
) : BaseRepository(), EtfRepository {

    override suspend fun getTopVolumeEtfs() =
        safeApiCallWithEnvelope { etfApiService.getTopVolumeEtfs() }
            .map { body ->
                TopVolumeEtfSnapshot(
                    items = body.data.orEmpty().map { item -> item.toDomain() },
                    timestamp = body.timestamp
                )
            }

    override suspend fun getEtfList(filter: EtfFilter, page: Int): BaseResult<EtfPage> =
        safeApiCall { etfApiService.getEtfList(filter.toRequest(), page) }
            .map { EtfPage(items = it.content.map { item -> item.toDomain() }, isLast = it.last) }

    override fun getLikedEtfList(): Flow<List<EtfLikeData>> =
        likedEtfDao.getLikedEtfs().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleLike(data: EtfLikeData): BaseResult<Boolean> {
        return try {
            val isCurrentlyLiked = likedEtfDao.isLiked(data.ticker)
            if (isCurrentlyLiked) {
                likedEtfDao.deleteLikedEtf(data.ticker)
            } else {
                likedEtfDao.insertLikedEtf(data.toLikedEntity())
            }
            BaseResult.Success(!isCurrentlyLiked)
        } catch (e: Exception) {
            BaseResult.Error(ApiError.unknownError(e.message ?: "관심 ETF 처리 중 오류가 발생했습니다"))
        }
    }

    override suspend fun getEtfDetail(ticker: String): BaseResult<EtfDetail> =
        safeApiCall { etfApiService.getEtfDetail(ticker) }.map { it.toDomain() }

    override suspend fun getEtfCluster(ticker: String): BaseResult<EtfClusterData> =
        safeApiCall { etfApiService.getEtfCluster(ticker) }.map { it.toDomain() }

    override suspend fun getEtfPriceHistory(ticker: String, startDate: String, endDate: String, size: Int): BaseResult<List<EtfPriceData>> =
        safeApiCall { etfApiService.getEtfPriceHistory(ticker, startDate, endDate, validDateRange = false, size = size) }
            .map { it.toDomain() }

    override suspend fun getMarketData(ticker: String): BaseResult<EtfMarketData> =
        safeApiCall { etfApiService.getMarketData(ticker) }.map { it.toDomain() }

}

private fun EtfFilter.toRequest() = EtfListRequest(
    riskType = riskType,
    strategy = strategy,
    sector = sector,
    dividendYield = dividendYield,
    dividendFrequency = dividendFrequency,
    isDerivatives = isDerivatives,
    isLeverage = isLeverage,
    isInverse = isInverse,
    perLow = perLow,
    perHigh = perHigh,
    pbrLow = pbrLow,
    pbrHigh = pbrHigh,
    roeLow = roeLow,
    roeHigh = roeHigh,
    commission = commission,
    aum = aum,
    sortedBy = sortedBy,
    searchName = searchName?.takeIf { it.isNotBlank() },
    isFavorite = isFavorite,
)
