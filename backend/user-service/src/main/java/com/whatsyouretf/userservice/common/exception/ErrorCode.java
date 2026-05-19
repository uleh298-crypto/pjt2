package com.whatsyouretf.userservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON002", "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON003", "리소스를 찾을 수 없습니다."),
    BIND_ERROR(HttpStatus.BAD_REQUEST, "COMMON4", "바인딩 에러가 발생했습니다"),
    ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "COMMON5", "파라미터가 유효하지 않습니다."),
    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH003", "만료된 토큰입니다."),
    OAUTH_FAILED(HttpStatus.BAD_REQUEST, "AUTH004", "소셜 로그인에 실패했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH005", "이메일 또는 비밀번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH006", "이메일 인증이 완료되지 않았습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH007", "비밀번호 확인이 일치하지 않습니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH008", "비밀번호 조건을 충족하지 않습니다."),
    UNAUTHENTICATED(HttpStatus.FORBIDDEN, "AUTH009", "권한이 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER003", "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER004", "비밀번호가 일치하지 않습니다."),
    ALREADY_FAVORITE(HttpStatus.CONFLICT, "USER005", "이미 관심 ETF에 추가되어 있습니다."),
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER006", "관심 ETF를 찾을 수 없습니다."),
    MYDATA_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "USER007", "마이데이터 연동이 필요합니다."),
    MYDATA_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER008", "마이데이터 동기화에 실패했습니다."),
    ALREADY_ACCEPTED_MYDATA(HttpStatus.BAD_REQUEST, "USER009", "이미 동의한 계정입니다."),

    // Social Account
    SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "SOCIAL001", "연동된 소셜 계정을 찾을 수 없습니다."),
    SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "SOCIAL002", "이미 연동된 소셜 계정입니다."),

    // News
    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "NEWS001", "뉴스를 찾을 수 없습니다."),
    NEWS_SCRAPE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NEWS002", "뉴스 수집에 실패했습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "NEWS003", "잘못된 날짜 범위입니다."),
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "NEWS004", "유효하지 않은 검색어입니다."),

    // AI Feedback
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "AI001", "AI 리뷰를 찾을 수 없습니다."),
    REVIEW_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI002", "AI 리뷰 생성에 실패했습니다."),
    INVALID_PORTFOLIO_FOR_REVIEW(HttpStatus.BAD_REQUEST, "AI003", "리뷰할 수 없는 포트폴리오입니다."),
    REVIEW_PROCESSING(HttpStatus.ACCEPTED, "AI004", "AI 분석이 진행 중입니다."),
    ALREADY_RATED(HttpStatus.CONFLICT, "AI005", "이미 평가한 리뷰입니다."),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI006", "AI 서비스를 일시적으로 사용할 수 없습니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI007", "요청 한도를 초과했습니다."),

    // Alert
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "ALERT001", "알림을 찾을 수 없습니다."),
    FCM_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "ALERT002", "유효하지 않은 FCM 토큰입니다."),
    FCM_TOKEN_ALREADY_EXISTS(HttpStatus.CONFLICT, "ALERT003", "이미 등록된 FCM 토큰입니다."),

    // ETF
    ETF_NOT_FOUND(HttpStatus.NOT_FOUND, "ETF001", "ETF를 찾을 수 없습니다."),
    ETF_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, "ETF002", "ETF 가격 정보를 찾을 수 없습니다."),
    ETF_COMPOSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "ETF003", "ETF 구성종목 정보를 찾을 수 없습니다."),
    SECTOR_CLUSTER_NOT_FOUND(HttpStatus.NOT_FOUND, "ETF004", "섹터 클러스터 정보를 찾을 수 없습니다."),

    // Stock
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK001", "종목을 찾을 수 없습니다."),

    // Portfolio
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "PORTFOLIO001", "포트폴리오를 찾을 수 없습니다."),
    PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PORTFOLIO002", "포트폴리오 접근 권한이 없습니다."),
    DUPLICATE_PORTFOLIO_NAME(HttpStatus.CONFLICT, "PORTFOLIO003", "이미 사용 중인 포트폴리오 이름입니다."),
    PORTFOLIO_ETF_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PORTFOLIO004", "포트폴리오 ETF 개수 제한을 초과했습니다."),
    INVALID_WEIGHT_SUM(HttpStatus.BAD_REQUEST, "PORTFOLIO005", "ETF 비중 합계가 100%가 아닙니다."),
    INVALID_PORTFOLIO_PERIOD(HttpStatus.BAD_REQUEST, "PORTFOLIO006", "유효하지 않은 투자 기간입니다."),

    // Simulation
    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIM001", "시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SIM002", "시뮬레이션 실행에 실패했습니다."),
    INVALID_SIMULATION_PERIOD(HttpStatus.BAD_REQUEST, "SIM003", "잘못된 시뮬레이션 기간입니다."),
    SIMULATION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SIM004", "시뮬레이션 개수 제한을 초과했습니다."),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE001", "파일 업로드에 실패했습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE002", "파일이 비어있습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE003", "파일 크기가 제한을 초과했습니다. (최대 5MB)"),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FILE004", "허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 허용)"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE005", "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE006", "파일 삭제에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
