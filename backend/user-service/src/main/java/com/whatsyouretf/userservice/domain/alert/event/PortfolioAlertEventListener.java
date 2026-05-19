package com.whatsyouretf.userservice.domain.alert.event;

import com.whatsyouretf.userservice.common.config.RabbitMQConfig;
import com.whatsyouretf.userservice.domain.alert.service.PortfolioAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 포트폴리오 알림 이벤트 리스너
 * data-service 에서 발행한 PORTFOLIO_ALERT 이벤트 수신
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioAlertEventListener {

    private final PortfolioAlertService portfolioAlertService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PORTFOLIO_ALERT)
    public void handlePortfolioAlertEvent(PortfolioAlertEvent event) {
        log.info("포트폴리오 알림 이벤트 수신: portfolioId={}, userId={}, change={}%, threshold={}%",
                event.portfolioId(), event.userId(),
                event.changeRate(), event.threshold());

        try {
            portfolioAlertService.processPortfolioAlert(event);
        } catch (Exception e) {
            log.error("포트폴리오 알림 처리 실패: portfolioId={}, error={}",
                    event.portfolioId(), e.getMessage(), e);
        }
    }
}
