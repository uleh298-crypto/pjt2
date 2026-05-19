package com.whatsyouretf.userservice.domain.alert.event;

import com.whatsyouretf.userservice.common.config.RabbitMQConfig;
import com.whatsyouretf.userservice.domain.alert.service.NewsAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 뉴스 이벤트 리스너
 * data-service에서 발행한 이벤트 수신
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsEventListener {

    private final NewsAlertService newsAlertService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NEWS_ALERT)
    public void handleNewsAnalyzedEvent(NewsAnalyzedEvent event) {
        log.info("뉴스 분석 이벤트 수신: newsId={}, eventType={}",
                event.getNewsId(), event.getEventType());

        try {
            int alertCount = newsAlertService.processNewsEvent(event);
            log.info("뉴스 알림 처리 완료: newsId={}, alertCount={}",
                    event.getNewsId(), alertCount);
        } catch (Exception e) {
            log.error("뉴스 알림 처리 실패: newsId={}, error={}",
                    event.getNewsId(), e.getMessage(), e);
        }
    }
}
