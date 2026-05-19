package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.PortfolioDetail
import com.d102.wye.domain.model.PortfolioIssue
import com.d102.wye.domain.model.PortfolioListItem
import com.d102.wye.domain.model.SavePortfolioParams

interface PortfolioRepository {

    suspend fun savePortfolio(params: SavePortfolioParams): BaseResult<Unit>

    suspend fun getPortfolioList(): BaseResult<List<PortfolioListItem>>

    suspend fun getPortfolioDetail(portfolioId: Long): BaseResult<PortfolioDetail>

    suspend fun deletePortfolio(portfolioId: Long): BaseResult<Unit>

    suspend fun updatePortfolio(portfolioId: Long, name: String): BaseResult<Unit>

    suspend fun getPortfolioIssues(portfolioId: Long): BaseResult<List<PortfolioIssue>>
}