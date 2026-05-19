package com.d102.wye.domain.common

/**
 * API 에러 정보
 *
 * domain/common에 위치하는 이유:
 * data(Repository), presentation(ViewModel) 양쪽에서 모두 사용하는 타입이고
 * 비즈니스 의미를 가진 에러 정보이므로 domain에 위치한다
 * Android import 없음 → 순수 Kotlin
 */
data class ApiError(
    val message: String = "알 수 없는 오류",
    val code: Int = -1,
    val type: ErrorType = ErrorType.UNKNOWN
) {
    enum class ErrorType {
        NETWORK,        // 네트워크 연결 실패 (IOException)
        TIMEOUT,        // 응답 시간 초과 (SocketTimeoutException)
        UNAUTHORIZED,   // 인증 실패 (401)
        FORBIDDEN,      // 권한 없음 (403)
        NOT_FOUND,      // 리소스 없음 (404)
        SERVER,         // 서버 에러 (5xx)
        UNKNOWN         // 그 외
    }

    companion object {
        fun getErrorType(code: Int?): ErrorType = when (code) {
            401  -> ErrorType.UNAUTHORIZED
            403  -> ErrorType.FORBIDDEN
            404  -> ErrorType.NOT_FOUND
            in 500..599 -> ErrorType.SERVER
            else -> ErrorType.UNKNOWN
        }

        fun networkError() = ApiError(
            message = "네트워크 연결을 확인해주세요",
            code = -1,
            type = ErrorType.NETWORK
        )

        fun timeoutError() = ApiError(
            message = "요청 시간이 초과되었습니다",
            code = -1,
            type = ErrorType.TIMEOUT
        )

        fun unknownError(message: String) = ApiError(
            message = message,
            code = -1,
            type = ErrorType.UNKNOWN
        )
    }
}