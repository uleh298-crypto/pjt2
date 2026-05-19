package com.whatsyouretf.userservice.domain.alert.service;

import com.whatsyouretf.userservice.domain.alert.entity.*;
import com.whatsyouretf.userservice.domain.alert.event.NewsAnalyzedEvent;
import com.whatsyouretf.userservice.domain.alert.repository.AlertMessageTemplateRepository;
import com.whatsyouretf.userservice.domain.alert.repository.AlertTypeRepository;
import com.whatsyouretf.userservice.domain.alert.repository.UserAlertRepository;
import com.whatsyouretf.userservice.domain.alert.repository.UserNotificationSettingRepository;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import com.whatsyouretf.userservice.domain.user.repository.UserFavoriteEtfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 뉴스 알림 서비스
 * AI 분석 완료된 뉴스에 대해 관심 ETF 사용자에게 알림 발송
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsAlertService {

    private final UserFavoriteEtfRepository userFavoriteEtfRepository;
    private final UserNotificationSettingRepository notificationSettingRepository;
    private final UserAlertRepository userAlertRepository;
    private final AlertTypeRepository alertTypeRepository;
    private final AlertMessageTemplateRepository alertMessageTemplateRepository;
    private final EtfRepository etfRepository;
    private final FcmService fcmService;

    private static final String ALERT_TYPE_CODE = "NEWS_ETF_INFLUENCE";
    private static final String SETTING_GROUP = "NEWS_NOTIFICATION";

    /**
     * 뉴스 알림 생성 및 푸시 발송
     */
    @Transactional
    public int processNewsEvent(NewsAnalyzedEvent event) {
        log.info("뉴스 알림 처리 시작: newsId={}, etfIds={}", event.getNewsId(), event.getEtfIds());

        if (event.getEtfIds() == null || event.getEtfIds().isEmpty()) {
            return 0;
        }

        // 1. AlertType 조회 (없으면 생성)
        AlertType alertType = getOrCreateAlertType();

        // 2. 메시지 템플릿 조회
        AlertMessageTemplate template = alertMessageTemplateRepository
                .findByAlertTypeCodeAndIsActiveTrue(ALERT_TYPE_CODE)
                .orElse(null);

        // 3. ETF 정보 조회
        Map<Long, Etf> etfMap = etfRepository.findAllById(event.getEtfIds()).stream()
                .collect(Collectors.toMap(Etf::getId, e -> e));

        // 4. 각 ETF에 대해 관심 사용자 조회 및 알림 생성
        Set<Long> notifiedUserIds = new HashSet<>();
        List<UserAlert> alertsToSave = new ArrayList<>();

        for (Long etfId : event.getEtfIds()) {
            Etf etf = etfMap.get(etfId);
            if (etf == null) continue;

            // 해당 ETF를 관심 등록한 사용자 조회
            List<UserFavoriteEtf> favorites = userFavoriteEtfRepository.findAllByEtfIdWithUser(etfId);

            for (UserFavoriteEtf favorite : favorites) {
                User user = favorite.getUser();

                // 이미 이 뉴스로 알림 받은 사용자는 스킵
                if (notifiedUserIds.contains(user.getId())) continue;

                // 알림 설정 확인
                if (!isNotificationEnabled(user.getId())) continue;

                // 알림 생성 (템플릿 사용)
                String title;
                String message;

                if (template != null) {
                    Map<String, String> variables = Map.of(
                            "etf_name", etf.getName(),
                            "news_summary", truncateMessage(event.getNewsSummary(), 200)
                    );
                    title = template.renderTitle(variables);
                    message = template.renderMessage(variables);
                } else {
                    // 템플릿 없으면 기본값 사용
                    title = String.format("관심 ETF [%s] 관련 뉴스", etf.getName());
                    message = truncateMessage(event.getNewsSummary(), 200);
                }

                UserAlert alert = UserAlert.builder()
                        .user(user)
                        .alertType(alertType)
                        .referenceType(ReferenceType.NEWS)
                        .referenceId(event.getNewsId())
                        .title(title)
                        .message(message)
                        .build();

                alertsToSave.add(alert);
                notifiedUserIds.add(user.getId());
            }
        }

        // 4. 알림 일괄 저장
        if (!alertsToSave.isEmpty()) {
            userAlertRepository.saveAll(alertsToSave);
            log.info("뉴스 알림 저장 완료: {}건", alertsToSave.size());

            // 5. FCM 푸시 비동기 발송
            sendPushNotificationsAsync(alertsToSave, event.getNewsId());
        }

        return alertsToSave.size();
    }

    /**
     * 푸시 알림 비동기 발송
     */
    @Async
    public void sendPushNotificationsAsync(List<UserAlert> alerts, Long newsId) {
        Map<String, String> data = Map.of(
                "type", "NEWS",
                "newsId", String.valueOf(newsId)
        );

        for (UserAlert alert : alerts) {
            try {
                fcmService.sendToUser(
                        alert.getUser().getId(),
                        alert.getTitle(),
                        alert.getMessage(),
                        data
                );
            } catch (Exception e) {
                log.error("FCM 발송 실패: userId={}, error={}", alert.getUser().getId(), e.getMessage());
            }
        }
    }

    /**
     * 알림 설정 확인
     */
    private boolean isNotificationEnabled(Long userId) {
        return notificationSettingRepository.findByUserIdAndAlertTypeCode(userId, ALERT_TYPE_CODE)
                .map(UserNotificationSetting::getIsEnabled)
                .orElse(true);
    }

    /**
     * AlertType 조회 또는 생성
     */
    private AlertType getOrCreateAlertType() {
        return alertTypeRepository.findById(ALERT_TYPE_CODE)
                .orElseGet(() -> {
                    AlertType newType = AlertType.builder()
                            .code(ALERT_TYPE_CODE)
                            .name("관심 ETF 뉴스 알림")
                            .category(AlertCategory.NEWS)
                            .settingGroup(SETTING_GROUP)
                            .description("관심 ETF에 영향을 주는 뉴스가 발생하면 알림을 보내드립니다.")
                            .isActive(true)
                            .displayOrder(10)
                            .build();
                    return alertTypeRepository.save(newType);
                });
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }
}
