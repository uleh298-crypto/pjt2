package com.d102.wye.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.d102.wye.data.local.entity.LikedEtfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedEtfDao {

    /**
     * 관심 ETF 전체 목록 구독
     * DB가 바뀌면 Flow가 자동으로 새 값을 emit
     */
    @Query("SELECT * FROM liked_etf")
    fun getLikedEtfs(): Flow<List<LikedEtfEntity>>

    /**
     * 관심 ETF 추가
     * 이미 존재하면 최신 가격 등 정보로 덮어씀 (REPLACE)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedEtf(entity: LikedEtfEntity)

    /**
     * 관심 ETF 삭제
     */
    @Query("DELETE FROM liked_etf WHERE ticker = :ticker")
    suspend fun deleteLikedEtf(ticker: String)

    /**
     * 특정 ETF가 관심 목록에 있는지 확인
     */
    @Query("SELECT COUNT(*) > 0 FROM liked_etf WHERE ticker = :ticker")
    suspend fun isLiked(ticker: String): Boolean

    /**
     * 전체 삭제
     */
    @Query("DELETE FROM liked_etf")
    suspend fun deleteAll()
}