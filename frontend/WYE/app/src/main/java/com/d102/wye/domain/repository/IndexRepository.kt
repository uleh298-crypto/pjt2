package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.IndexPoint

interface IndexRepository {
    suspend fun getIndex(marketType: String, startDate: String, endDate: String): BaseResult<List<IndexPoint>>
}
