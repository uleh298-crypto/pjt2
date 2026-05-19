package com.whatsyouretf.userservice.domain.alert.service;

import com.whatsyouretf.userservice.domain.alert.entity.*;
import com.whatsyouretf.userservice.domain.alert.event.PortfolioAlertEvent;
import com.whatsyouretf.userservice.domain.alert.repository.AlertTypeRepository;
import com.whatsyouretf.userservice.domain.alert.repository.UserAlertRepository;
import com.whatsyouretf.userservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 포트폴리오 가치 변동 알림 서비스
 * data-service 에서 발행한 PORTFOLIO_ALERT 이벤트를 처리하여 FCM 발송
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioAlertService {

    private final FcmService fcmService;
    private final UserAlertRepository userAlertRepository;
    private final AlertTypeRepository alertTypeRepository;

    private static final String ALERT_TYPE_CODE = "PORTFOLIO_CHANGE";
    private static final String SETTING_GROUP = "PORTFOLIO_NOTIFICATION";

    /**
     * 포트폴리오 변동 알림 처리
     * UserAlert 저장 + FCM 푸시 발송
     */
    @Transactional
    public void processPortfolioAlert(PortfolioAlertEvent event) {
        AlertType alertType = getOrCreateAlertType();

        String title = String.format("[%s] %d%% %s",
                event.portfolioName(), event.threshold(), event.direction());
        String message = String.format("%s 포트폴리오가 장 시작 대비 %.2f%% %s했습니다.",
                event.portfolioName(), Math.abs(event.changeRate()), event.direction());

        // 알림 저장
        UserAlert alert = UserAlert.builder()
                .user(User.of(event.userId()))
                .alertType(alertType)
                .referenceType(ReferenceType.PORTFOLIO)
                .referenceId(event.portfolioId())
                .title(title)
                .message(message)
                .build();

        userAlertRepository.save(alert);

        // FCM 발송
        Map<String, String> data = Map.of(
                "type", "PORTFOLIO",
                "portfolioId", String.valueOf(event.portfolioId())
        );

        boolean sent = fcmService.sendToUser(event.userId(), title, message, data);

        log.info("포트폴리오 알림 처리 완료: userId={}, portfolioId={}, change={}%, sent={}",
                event.userId(), event.portfolioId(), event.changeRate(), sent);
    }

    private AlertType getOrCreateAlertType() {
        return alertTypeRepository.findById(ALERT_TYPE_CODE)
                .orElseGet(() -> {
                    AlertType newType = AlertType.builder()
                            .code(ALERT_TYPE_CODE)
                            .name("포트폴리오 가치 변동 알림")
                            .category(AlertCategory.PORTFOLIO)
                            .settingGroup(SETTING_GROUP)
                            .description("포트폴리오 가치가 장 시작 대비 5% 또는 10% 이상 변동되면 알림을 보내드립니다.")
                            .isActive(true)
                            .displayOrder(20)
                            .build();
                    return alertTypeRepository.save(newType);
                });
    }
}
