package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.StockApiService
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock
import com.d102.wye.domain.repository.StockRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val stockApiService: StockApiService,
) : BaseRepository(), StockRepository {

    override suspend fun getStock(ticker: String): BaseResult<Stock> {
        val detailResult = safeApiCall { stockApiService.getStockDetail(ticker) }
        val etfsResult   = safeApiCall { stockApiService.getContainedEtfs(ticker) }
        return when (detailResult) {
            is BaseResult.Success -> {
                val etfs = (etfsResult as? BaseResult.Success)?.data
                    ?.map { it.toDomain() } ?: emptyList()
                BaseResult.Success(detailResult.data.toDomain(etfs))
            }
            is BaseResult.Error -> detailResult
        }
    }

    override suspend fun getRelatedStocks(ticker: String): BaseResult<List<RelatedStock>> =
        safeApiCall { stockApiService.getRelatedStocks(ticker) }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getTags(ticker: String): BaseResult<List<String>> =
        safeApiCall { stockApiService.getTags(ticker) }
}
