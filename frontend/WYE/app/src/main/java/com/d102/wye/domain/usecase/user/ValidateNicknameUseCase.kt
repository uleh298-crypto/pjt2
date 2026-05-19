package com.d102.wye.domain.usecase.user

class ValidateNicknameUseCase {

    operator fun invoke(nickname: String): String? = when {
        nickname.isEmpty() -> null
        nickname.length !in 2..20 -> "닉네임은 2자 이상 20자 이하로 입력해 주세요."
        !NICKNAME_REGEX.matches(nickname) -> "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."
        else -> null
    }

    companion object {
        private val NICKNAME_REGEX = Regex("^[가-힣A-Za-z0-9]+$")
    }
}
