package com.d102.wye.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.d102.wye.data.local.entity.EtfPriceHistoryEntity

@Dao
interface EtfPriceHistoryDao {

    /**
     * 가격 이력 저장 (중복 시 교체)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<EtfPriceHistoryEntity>)

    /**
     * ticker 기준 전체 가격 이력 조회 (날짜 오름차순)
     */
    @Query("SELECT * FROM etf_price_history WHERE ticker = :ticker ORDER BY date ASC")
    suspend fun getByTicker(ticker: String): List<EtfPriceHistoryEntity>

    /**
     * ticker 기준 특정 기간 가격 이력 조회
     */
    @Query(
        """
        SELECT * FROM etf_price_history 
        WHERE ticker = :ticker 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC
    """
    )
    suspend fun getByTickerAndDateRange(
        ticker: String,
        startDate: String,
        endDate: String
    ): List<EtfPriceHistoryEntity>

    /**
     * ticker 기준 데이터 존재 여부 확인
     */
    @Query("SELECT COUNT(*) FROM etf_price_history WHERE ticker = :ticker")
    suspend fun countByTicker(ticker: String): Int

    /**
     * ticker 기준 삭제 (ETF 제거 시)
     */
    @Query("DELETE FROM etf_price_history WHERE ticker = :ticker")
    suspend fun deleteByTicker(ticker: String)


    @Query("SELECT MAX(date) FROM etf_price_history WHERE ticker = :ticker")
    suspend fun getLastCachedDate(ticker: String): String?
}