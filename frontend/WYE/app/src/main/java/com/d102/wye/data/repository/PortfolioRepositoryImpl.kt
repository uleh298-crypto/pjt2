package com.d102.wye.data.repository

import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.PortfolioApiService
import com.d102.wye.data.remote.dto.request.EtfCount
import com.d102.wye.data.remote.dto.request.SavePortfolioRequest
import com.d102.wye.data.remote.dto.request.UpdatePortfolioRequest
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.PortfolioDetail
import com.d102.wye.domain.model.PortfolioIssue
import com.d102.wye.domain.model.PortfolioListItem
import com.d102.wye.domain.model.SavePortfolioParams
import com.d102.wye.domain.repository.PortfolioRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val portfolioApiService: PortfolioApiService
) : BaseRepository(), PortfolioRepository {

    override suspend fun savePortfolio(params: SavePortfolioParams): BaseResult<Unit> =
        safeApiCallWithoutData {
            portfolioApiService.savePortfolio(
                SavePortfolioRequest(
                    portfolioName = params.portfolioName,
                    portfolioType = params.investType.name,
                    investAmount = params.investAmount,
                    investPeriod = params.investPeriod,
                    etfs = params.etfs.map { EtfCount(ticker = it.ticker, counts = it.counts) }
                )
            )
        }

    override suspend fun getPortfolioList(): BaseResult<List<PortfolioListItem>> =
        safeApiCall { portfolioApiService.getPortfolioList() }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getPortfolioDetail(portfolioId: Long): BaseResult<PortfolioDetail> =
        safeApiCall { portfolioApiService.getPortfolioDetail(portfolioId) }
            .map { it.toDomain() }

    override suspend fun deletePortfolio(portfolioId: Long): BaseResult<Unit> =
        safeApiCallWithoutData { portfolioApiService.deletePortfolio(portfolioId) }

    override suspend fun updatePortfolio(portfolioId: Long, name: String): BaseResult<Unit> =
        safeApiCallWithoutData {
            portfolioApiService.updatePortfolio(
                UpdatePortfolioRequest(portfolioId = portfolioId, name = name)
            )
        }

    override suspend fun getPortfolioIssues(portfolioId: Long): BaseResult<List<PortfolioIssue>> =
        safeApiCall { portfolioApiService.getPortfolioIssues(portfolioId) }
            .map { dtos -> dtos.map { it.toDomain() } }
}
