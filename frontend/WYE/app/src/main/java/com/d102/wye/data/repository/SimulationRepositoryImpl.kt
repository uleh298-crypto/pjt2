package com.d102.wye.data.repository

import com.d102.wye.data.local.dao.EtfPriceHistoryDao
import com.d102.wye.data.local.entity.EtfPriceHistoryEntity
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.SimulationApiService
import com.d102.wye.data.remote.dto.request.AiEtfInfo
import com.d102.wye.data.remote.dto.request.AiPortfolioData
import com.d102.wye.data.remote.dto.request.AiReviewRequest
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.AiReviewResult
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.EtfPricePoint
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject

class SimulationRepositoryImpl @Inject constructor(
    private val simulationApiService: SimulationApiService,
    private val priceHistoryDao: EtfPriceHistoryDao
) : SimulationRepository {

    // ─── API ─────────────────────────────────────────────────────────────────

    override suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?,
        page: Int
    ): BaseResult<Map<String, EtfPriceHistory>> = runCatching {
        coroutineScope {
            val results = tickers.map { ticker ->
                async {
                    runCatching {
                        ticker to fetchAllPages(ticker, startDate, endDate)
                    }.getOrElse { e ->
                        Timber.e("[API] ticker=$ticker 조회 실패 | ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            val successMap = results.filterNotNull().toMap()
            Timber.d("[API] 전체 조회 완료 | 성공=${successMap.keys} | 실패=${tickers - successMap.keys.toSet()}")
            BaseResult.Success(successMap)
        }
    }.getOrElse { e ->
        Timber.e("[API] 가격 이력 전체 조회 실패 | ${e.message}")
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "가격 이력 조회 실패"))
    }

    private suspend fun fetchAllPages(
        ticker: String,
        startDate: String?,
        endDate: String?
    ): EtfPriceHistory = supervisorScope {
        // 1. 첫 페이지 호출 → data 언래핑
        val firstPageData = simulationApiService.getEtfPriceHistory(
            ticker = ticker,
            startDate = startDate,
            endDate = endDate,
            page = 0
        ).data ?: throw IllegalStateException("ticker=$ticker 응답 data가 null")

        Timber.d("[API] ticker=$ticker | totalPages=${firstPageData.totalPages} | totalElements=${firstPageData.totalElements}")

        if (firstPageData.last || firstPageData.totalPages <= 1) {
            return@supervisorScope firstPageData.toDomain(ticker)
        }

        // 2. 나머지 페이지 병렬 호출
        val remainingPages = (1 until firstPageData.totalPages).map { pageIndex ->
            async {
                simulationApiService.getEtfPriceHistory(
                    ticker = ticker,
                    startDate = startDate,
                    endDate = endDate,
                    page = pageIndex
                ).data?.also {
                    Timber.d("[API] ticker=$ticker | page=$pageIndex 완료 | count=${it.content.size}")
                } ?: throw IllegalStateException("ticker=$ticker page=$pageIndex 응답 data가 null")
            }
        }.awaitAll()

        // 3. 전체 content 합산 후 toDomain
        val allContent = firstPageData.content + remainingPages.flatMap { it.content }
        Timber.d("[API] ticker=$ticker | 전체 합산 완료 | totalCount=${allContent.size}")

        firstPageData.copy(content = allContent, last = true).toDomain(ticker)
    }

    override suspend fun getEtfDividendHistories(
        tickers: List<String>,
        startDate: String?,
        endDate: String?
    ): BaseResult<Map<String, EtfDividendHistory>> = runCatching {
        supervisorScope {
            val results = tickers.map { ticker ->
                async {
                    runCatching {
                        val data = simulationApiService.getEtfDividendHistory(
                            ticker = ticker,
                            startDate = startDate,
                            endDate = endDate
                        ).data ?: throw IllegalStateException("ticker=$ticker 배당금 응답 data가 null")
                        ticker to data.toDomain(ticker)
                    }.getOrElse { e ->
                        Timber.e("[API] ticker=$ticker 배당금 조회 실패 | ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            BaseResult.Success(results.filterNotNull().toMap())
        }
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "배당금 이력 조회 실패"))
    }

    override suspend fun getAiPortfolioReview(
        totalAmount: Long,
        investmentType: InvestmentType,
        portfolios: List<Portfolio>
    ): BaseResult<AiReviewResult> = runCatching {
        val data = simulationApiService.getAiPortfolioReview(
            AiReviewRequest(
                portfolio = AiPortfolioData(
                    totalAmount = totalAmount,
                    investmentType = investmentType.name,
                    etfs = portfolios.filter { it.weightPercent > 0 }.map { portfolio ->
                        AiEtfInfo(
                            ticker = portfolio.ticker,
                            name = portfolio.name,
                            weight = portfolio.weightPercent
                        )
                    }
                )
            )
        ).data ?: throw IllegalStateException("AI 진단 응답 data가 null")

        BaseResult.Success(
            AiReviewResult(
                mainTitle = data.headline,
                subTitle = data.subHeadline,
                tags = data.keywords,
                feedback = data.analysis
            )
        )
    }.getOrElse { e ->
        Timber.e("[API] AI 진단 실패 | ${e.message}")
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "AI 진단 실패"))
    }

    override suspend fun getPresetList(): BaseResult<List<EtfBundle>> = runCatching {
        val data = simulationApiService.getPresetList().data
            ?: throw IllegalStateException("프리셋 목록 응답 data가 null")
        BaseResult.Success(data.map { it.toDomain() })
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "프리셋 목록 조회 실패"))
    }

    override suspend fun getPresetDetail(presetId: Int): BaseResult<EtfBundleDetail> = runCatching {
        val data = simulationApiService.getPresetDetail(presetId).data
            ?: throw IllegalStateException("프리셋 상세 응답 data가 null")
        BaseResult.Success(data.toDomain())
    }.getOrElse { e ->
        BaseResult.Error(ApiError(code = -1, message = e.message ?: "프리셋 상세 조회 실패"))
    }

    // ─── 로컬 DB 캐시 ─────────────────────────────────────────────────────────

    override suspend fun savePriceHistories(histories: Map<String, EtfPriceHistory>) {
        val entities = histories.flatMap { (ticker, history) ->
            history.content.map { point ->
                EtfPriceHistoryEntity(
                    ticker = ticker,
                    date = point.date,
                    stockPrice = point.stockPrice,
                    dailyReturn = point.dailyReturn
                )
            }
        }
        Timber.d("[DB] 저장 시작 | 총 ${entities.size}건")
        priceHistoryDao.insertAll(entities)
        Timber.d("[DB] 저장 완료")
    }

    override suspend fun getCachedPriceHistories(
        tickers: List<String>
    ): Map<String, EtfPriceHistory> {
        return tickers.associateWith { ticker ->
            val entities = priceHistoryDao.getByTicker(ticker)
            EtfPriceHistory(
                ticker = ticker,
                content = entities.map { entity ->
                    EtfPricePoint(
                        date = entity.date,
                        stockPrice = entity.stockPrice,
                        dailyReturn = entity.dailyReturn
                    )
                },
                totalElements = entities.size,
                totalPages = 1,
                last = true
            )
        }.filter { it.value.content.isNotEmpty() }
    }

    override suspend fun hasCachedPriceHistory(ticker: String): Boolean =
        priceHistoryDao.countByTicker(ticker) > 0

    override suspend fun deleteCachedPriceHistory(ticker: String) =
        priceHistoryDao.deleteByTicker(ticker)

    override suspend fun getLastCachedDate(ticker: String): String? =
        priceHistoryDao.getLastCachedDate(ticker)
}