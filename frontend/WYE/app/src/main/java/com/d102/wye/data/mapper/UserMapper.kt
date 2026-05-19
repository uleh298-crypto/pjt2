package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.FavoriteEtfListResponse
import com.d102.wye.data.remote.dto.response.FavoriteEtfResponse
import com.d102.wye.data.remote.dto.response.MyDataHoldingResponse
import com.d102.wye.data.remote.dto.response.SocialAccountResponse
import com.d102.wye.data.remote.dto.response.UserProfileResponse
import com.d102.wye.domain.model.FavoriteEtf
import com.d102.wye.domain.model.FavoriteEtfList
import com.d102.wye.domain.model.MyDataHolding
import com.d102.wye.domain.model.SocialAccount
import com.d102.wye.domain.model.UserProfile

fun UserProfileResponse.toDomain() = UserProfile(
    id = id ?: 0L,
    email = email.orEmpty(),
    nickname = nickname.orEmpty(),
    profileImage = profileImage,
    role = role.orEmpty(),
    isActive = isActive ?: false,
    lastLoginAt = lastLoginAt,
    createdAt = createdAt.orEmpty(),
    // PATCH /users/me 응답에는 socialAccounts가 없어 null일 수 있다.
    socialAccounts = socialAccounts.orEmpty().map { it.toDomain() }
)

fun SocialAccountResponse.toDomain() = SocialAccount(
    provider = provider,
    email = email,
    isPrimary = isPrimary,
    linkedAt = linkedAt
)

fun FavoriteEtfListResponse.toDomain() = FavoriteEtfList(
    favorites = favorites.map { it.toDomain() },
    totalCount = totalCount
)

fun FavoriteEtfResponse.toDomain() = FavoriteEtf(
    ticker = ticker,
    name = name,
    riskType = riskType,
    assetManager = assetManager,
    currentPrice = currentPrice.toLong(),
    changeRate = changeRate,
    favoritedAt = favoritedAt
)

fun MyDataHoldingResponse.toDomain() = MyDataHolding(
    ticker = ticker,
    counts = counts,
)
