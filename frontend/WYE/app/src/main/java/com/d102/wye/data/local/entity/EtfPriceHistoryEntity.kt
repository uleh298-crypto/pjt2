package com.d102.wye.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * ETF 가격 이력 캐시 테이블
 *
 * - ETF 추가 시 API 호출 후 저장
 * - 슬라이더 조작 시 여기서 읽어서 로컬 계산
 * - 복합 PK: (ticker, date) → 같은 날짜 중복 저장 방지
 */
@Entity(
    tableName = "etf_price_history",
    primaryKeys = ["ticker", "date"],
    indices = [Index(value = ["ticker"])]
)
data class EtfPriceHistoryEntity(
    val ticker: String,
    val date: String,        // "2026-01-15"
    val stockPrice: Long,    // 종가 (원)
    val dailyReturn: Double  // 전일 대비 수익률 (%)
)