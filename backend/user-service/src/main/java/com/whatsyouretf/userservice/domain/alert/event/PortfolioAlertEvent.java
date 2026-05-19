package com.whatsyouretf.userservice.domain.alert.event;

/**
 * 포트폴리오 가치 변동 알림 이벤트
 * data-service 에서 발행 → user-service 에서 수신하여 FCM 발송
 */
public record PortfolioAlertEvent(
        String eventType,
        Long portfolioId,
        Long userId,
        String portfolioName,
        Double changeRate,
        Integer threshold,
        String direction
) {
}
