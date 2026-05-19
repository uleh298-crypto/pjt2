package com.d102.wye.domain.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.FavoriteEtfList
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.model.MyDataHolding
import com.d102.wye.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /** 관심 ETF 추가/삭제 후 count 재조회 등에 사용할 변경 이벤트 */
    val favoriteEtfChanged: Flow<Unit>

    /** 현재 로그인한 사용자의 프로필 정보를 조회한다. */
    suspend fun getMyProfile(): BaseResult<UserProfile>

    /** 현재 로그인한 사용자의 관심 ETF 목록을 조회한다. */
    suspend fun getFavoriteEtfs(sort: FavoriteEtfSort = FavoriteEtfSort.RECENT): BaseResult<FavoriteEtfList>

    /** 특정 ETF가 관심 ETF에 등록되어 있는지 확인한다. */
    suspend fun checkFavoriteEtf(ticker: String): BaseResult<Boolean>

    /** 특정 ETF를 관심 ETF에 추가한다. */
    suspend fun addFavoriteEtf(ticker: String): BaseResult<Unit>

    /** 특정 ETF를 관심 ETF에서 삭제한다. */
    suspend fun deleteFavoriteEtf(ticker: String): BaseResult<Unit>

    /** 변경된 필드만 포함해 내 프로필을 수정한다. */
    suspend fun updateMyProfile(
        nickname: String? = null,
        profileImage: String? = null
    ): BaseResult<UserProfile>

    /** 프로필 이미지를 업로드하고 최신 프로필을 반환한다. */
    suspend fun uploadProfileImage(imageUri: String): BaseResult<UserProfile>

    /** 프로필 이미지를 삭제하고 최신 프로필을 반환한다. */
    suspend fun deleteProfileImage(): BaseResult<UserProfile>

    /** 마이데이터 동의 여부를 확인한다. */
    suspend fun getMyDataAccepted(): BaseResult<Boolean>

    /** 마이데이터 연동에 동의한다. */
    suspend fun acceptMyData(): BaseResult<Unit>

    /** 현재 시점의 마이데이터 보유 ETF 목록을 조회한다. */
    suspend fun getMyDataHoldings(): BaseResult<List<MyDataHolding>>
}
