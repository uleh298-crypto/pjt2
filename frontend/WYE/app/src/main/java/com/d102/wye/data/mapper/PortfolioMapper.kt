package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.PortfolioDetail as PortfolioDetailDto
import com.d102.wye.data.remote.dto.response.PortfolioCount as PortfolioCountDto
import com.d102.wye.data.remote.dto.response.PortfolioEtfDto
import com.d102.wye.data.remote.dto.response.PortfolioIssueDto
import com.d102.wye.data.remote.dto.response.PortfolioListItemDto
import com.d102.wye.domain.model.PortfolioCount
import com.d102.wye.domain.model.PortfolioDetail
import com.d102.wye.domain.model.PortfolioEtf
import com.d102.wye.domain.model.PortfolioIssue
import com.d102.wye.domain.model.PortfolioListItem
import com.d102.wye.domain.state.InvestmentType

fun PortfolioCountDto.toDomain() = PortfolioCount(
    ticker = ticker,
    counts = counts,
    etfName = etfName
)

fun PortfolioDetailDto.toDomain() = PortfolioDetail(
    portfolioId = portfolioId,
    portfolioName = portfolioName,
    counts = counts.map { it.toDomain() },
    investAmount = investAmount,
    createdAt = createdAt.take(10),
    portfolioType = when (portfolioType) {
        "REGULAR_SAVING" -> InvestmentType.REGULAR_SAVING
        else -> InvestmentType.LUMP_SUM
    }
)

fun PortfolioEtfDto.toDomain() = PortfolioEtf(
    ticker = ticker,
    name = name
)

fun PortfolioListItemDto.toDomain() = PortfolioListItem(
    portfolioId = portfolioId,
    title = title,
    createdAt = createdAt.take(10),
    etfList = etfList.map { it.toDomain() },
    totalReturn = totalReturn,
    isMyData = isMyData
)

/** 포트폴리오 이슈 DTO를 도메인 모델로 변환한다. */
fun PortfolioIssueDto.toDomain() = PortfolioIssue(
    localDate = localDate,
    title = title,
    description = description
)
