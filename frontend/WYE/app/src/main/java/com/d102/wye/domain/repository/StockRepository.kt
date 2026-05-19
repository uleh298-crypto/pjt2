package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock

interface StockRepository {
    suspend fun getStock(ticker: String): BaseResult<Stock>
    suspend fun getRelatedStocks(ticker: String): BaseResult<List<RelatedStock>>
    suspend fun getTags(ticker: String): BaseResult<List<String>>
}
