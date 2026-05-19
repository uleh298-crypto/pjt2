package com.whatsyouretf.userservice.domain.alert.service;

import com.google.firebase.messaging.*;
import com.whatsyouretf.userservice.domain.alert.entity.FcmToken;
import com.whatsyouretf.userservice.domain.alert.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FCM 푸시 알림 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 단일 사용자에게 푸시 알림 발송
     */
    public boolean sendToUser(Long userId, String title, String body) {
        return sendToUser(userId, title, body, null);
    }

    /**
     * 단일 사용자에게 푸시 알림 발송 (데이터 포함)
     */
    public boolean sendToUser(Long userId, String title, String body, java.util.Map<String, String> data) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("FCM 토큰 없음: userId={}", userId);
            return false;
        }

        boolean success = false;
        for (FcmToken fcmToken : tokens) {
            try {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setNotification(AndroidNotification.builder()
                                        .setSound("default")
                                        .build())
                                .build());

                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
                log.debug("FCM 발송 성공: userId={}, response={}", userId, response);
                success = true;
            } catch (FirebaseMessagingException e) {
                handleFcmError(fcmToken, e);
            }
        }

        return success;
    }

    /**
     * 여러 사용자에게 푸시 알림 발송
     */
    public int sendToUsers(List<Long> userIds, String title, String body) {
        return sendToUsers(userIds, title, body, null);
    }

    /**
     * 여러 사용자에게 푸시 알림 발송 (데이터 포함)
     */
    public int sendToUsers(List<Long> userIds, String title, String body, java.util.Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            if (sendToUser(userId, title, body, data)) {
                successCount++;
            }
        }

        log.info("FCM 일괄 발송 완료: 대상={}, 성공={}", userIds.size(), successCount);
        return successCount;
    }

    /**
     * 토큰 목록으로 직접 발송 (Multicast)
     */
    public int sendMulticast(List<String> tokens, String title, String body, java.util.Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }

        // Firebase는 한 번에 최대 500개 토큰만 지원
        int batchSize = 500;
        int successCount = 0;

        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(messageBuilder.build());
                successCount += response.getSuccessCount();

                if (response.getFailureCount() > 0) {
                    log.warn("FCM Multicast 실패: {} / {}", response.getFailureCount(), batch.size());
                }
            } catch (FirebaseMessagingException e) {
                log.error("FCM Multicast 오류: {}", e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * FCM 오류 처리 (만료된 토큰 비활성화)
     */
    private void handleFcmError(FcmToken fcmToken, FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        if (errorCode == MessagingErrorCode.UNREGISTERED ||
                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            // 토큰 무효화
            fcmToken.deactivate();
            fcmTokenRepository.save(fcmToken);
            log.info("FCM 토큰 비활성화: userId={}, reason={}", fcmToken.getUser().getId(), errorCode);
        } else {
            log.error("FCM 발송 실패: userId={}, error={}", fcmToken.getUser().getId(), e.getMessage());
        }
    }
}
