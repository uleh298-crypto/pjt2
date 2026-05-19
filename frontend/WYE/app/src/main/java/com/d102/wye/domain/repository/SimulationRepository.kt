package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.AiReviewResult
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.state.InvestmentType

interface SimulationRepository {

    // ─── API ─────────────────────────────────────────────────────────────────

    suspend fun getEtfPriceHistories(
        tickers: List<String>,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 0
    ): BaseResult<Map<String, EtfPriceHistory>>

    suspend fun getEtfDividendHistories(
        tickers: List<String>,
        startDate: String? = null,
        endDate: String? = null
    ): BaseResult<Map<String, EtfDividendHistory>>

    suspend fun getAiPortfolioReview(
        totalAmount: Long,
        investmentType: InvestmentType,
        portfolios: List<Portfolio>
    ): BaseResult<AiReviewResult>


    // ─── 로컬 DB 캐시 ─────────────────────────────────────────────────────────

    /**
     * API로 받은 가격 이력을 로컬 DB에 저장
     */
    suspend fun savePriceHistories(histories: Map<String, EtfPriceHistory>)

    /**
     * 로컬 DB에서 가격 이력 조회
     * ETF 추가 후 슬라이더 조작 시 여기서 읽음
     */
    suspend fun getCachedPriceHistories(tickers: List<String>): Map<String, EtfPriceHistory>

    /**
     * 로컬 DB에 해당 ticker 데이터가 있는지 확인
     */
    suspend fun hasCachedPriceHistory(ticker: String): Boolean

    /**
     * ETF 제거 시 로컬 DB 캐시도 삭제
     */
    suspend fun deleteCachedPriceHistory(ticker: String)

    /**
     * 최근에 저장된 날짜 조회
     */
    suspend fun getLastCachedDate(ticker: String): String?

    suspend fun getPresetList(): BaseResult<List<EtfBundle>>
    suspend fun getPresetDetail(presetId: Int): BaseResult<EtfBundleDetail>

}