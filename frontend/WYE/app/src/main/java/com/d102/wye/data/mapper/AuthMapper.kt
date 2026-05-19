package com.d102.wye.data.mapper

import com.d102.wye.data.remote.dto.response.TokenResponse
import com.d102.wye.data.remote.dto.response.UserResponse
import com.d102.wye.domain.model.LoginProvider
import com.d102.wye.domain.model.TokenPair
import com.d102.wye.domain.model.UserInfo

/**
 * 인증 관련 Mapper
 */
fun TokenResponse.toDomain() = TokenPair(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    isNewUser = isNewUser,
    user = user.toDomain()
)

fun UserResponse.toDomain() = UserInfo(
    id = id,
    email = email,
    nickname = nickname,
    profileImage = profileImage,
    loginProvider = LoginProvider.from(loginProvider)
)
