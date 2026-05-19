package com.whatsyouretf.userservice.domain.alert.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.alert.dto.*;
import com.whatsyouretf.userservice.domain.alert.entity.*;
import com.whatsyouretf.userservice.domain.alert.repository.*;
import com.whatsyouretf.userservice.domain.alert.service.AlertService;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final UserAlertRepository userAlertRepository;
    private final AlertTypeRepository alertTypeRepository;
    private final UserNotificationSettingRepository notificationSettingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final EtfRepository etfRepository;

    private static final int ALERT_DAYS = 7;

    /** 설정 그룹별 표시 정보 */
    private static final Map<String, String[]> SETTING_GROUP_INFO = Map.of(
            "APP_NOTIFICATION", new String[]{"앱 알림", "중요한 공지사항과 소식을 놓치지 마세요."},
            "ETF_LISTING", new String[]{"ETF 상장 알림", "관심 있는 ETF의 신규 상장 소식을 받아보세요."},
            "ETF_DELISTING", new String[]{"ETF 상장 폐지 알림", "관심 있는 ETF의 상장 폐지 소식을 받아보세요."},
            "PORTFOLIO_REBALANCING", new String[]{"포트폴리오 리밸런싱 알림", "포트폴리오 리밸런싱 될 때 알려드려요."},
            "PORTFOLIO_RETURN", new String[]{"포트폴리오 수익률 알림", "수익률에 큰 변화가 생기면 즉시 알려드려요."},
            "NEWS_NOTIFICATION", new String[]{"뉴스 수신 알림", "관심있는 ETF와 관련된 뉴스를 받아보세요."}
    );

    @Override
    public AlertListResponse getAlerts(Long userId, String category) {
        LocalDateTime since = LocalDateTime.now().minusDays(ALERT_DAYS);

        List<UserAlert> alertList;
        if (category == null || category.equalsIgnoreCase("all")) {
            alertList = userAlertRepository.findRecentByUserId(userId, since);
        } else {
            try {
                AlertCategory alertCategory = AlertCategory.valueOf(category.toUpperCase());
                alertList = userAlertRepository.findRecentByUserIdAndCategory(userId, alertCategory, since);
            } catch (IllegalArgumentException e) {
                alertList = userAlertRepository.findRecentByUserId(userId, since);
            }
        }

        // ETF 참조 알림의 ticker 조회
        Map<Long, String> etfTickerMap = buildEtfTickerMap(alertList);

        List<AlertListResponse.AlertItem> alerts = alertList.stream()
                .map(alert -> AlertListResponse.AlertItem.from(alert, etfTickerMap))
                .toList();

        long unreadCount = userAlertRepository.countByUserIdAndIsReadFalse(userId);

        return AlertListResponse.builder()
                .alerts(alerts)
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 알림 목록에서 ETF 참조 ID들의 ticker 맵 생성
     */
    private Map<Long, String> buildEtfTickerMap(List<UserAlert> alertList) {
        // ETF 타입 알림의 referenceId 수집
        Set<Long> etfIds = alertList.stream()
                .filter(a -> a.getReferenceType() == ReferenceType.ETF && a.getReferenceId() != null)
                .map(UserAlert::getReferenceId)
                .collect(Collectors.toSet());

        if (etfIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // ETF ID -> stockCode 맵 생성
        return etfRepository.findAllById(etfIds).stream()
                .collect(Collectors.toMap(Etf::getId, Etf::getStockCode));
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = userAlertRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long alertId) {
        UserAlert alert = userAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
        alert.markAsRead();
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return userAlertRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        UserAlert alert = userAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
        userAlertRepository.delete(alert);
    }

    @Override
    @Transactional
    public int deleteAllReadAlerts(Long userId) {
        return userAlertRepository.deleteAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void registerFcmToken(Long userId, FcmTokenRequest request) {
        if (request.getDeviceType() == null) {
            throw new BusinessException(ErrorCode.FCM_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 해당 사용자+디바이스 타입으로 기존 토큰 조회
        FcmToken existingUserToken = fcmTokenRepository
                .findByUserIdAndDeviceType(userId, request.getDeviceType())
                .orElse(null);

        if (existingUserToken != null) {
            // 기존 토큰이 있으면 업데이트
            existingUserToken.updateToken(request.getToken());
            return;
        }

        // 2. 다른 사용자가 같은 토큰을 사용 중인지 확인 (기기 이전)
        FcmToken tokenUsedByOther = fcmTokenRepository.findByToken(request.getToken()).orElse(null);
        if (tokenUsedByOther != null) {
            // 다른 사용자의 토큰 삭제 (기기 교체)
            fcmTokenRepository.delete(tokenUsedByOther);
            fcmTokenRepository.flush();
        }

        // 3. 새 토큰 등록
        FcmToken newToken = FcmToken.builder()
                .user(user)
                .token(request.getToken())
                .deviceType(request.getDeviceType())
                .build();
        fcmTokenRepository.save(newToken);
    }

    @Override
    @Transactional
    public void deleteFcmToken(Long userId, String token) {
        fcmTokenRepository.deleteByUserIdAndToken(userId, token);
    }

    @Override
    @Transactional
    public void deleteAllFcmTokens(Long userId) {
        fcmTokenRepository.deleteAllByUserId(userId);
    }

    @Override
    public AlertTypeListResponse getAlertTypes() {
        List<AlertType> alertTypes = alertTypeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        List<AlertTypeListResponse.AlertTypeItem> items = alertTypes.stream()
                .map(AlertTypeListResponse.AlertTypeItem::from)
                .toList();

        return AlertTypeListResponse.builder()
                .alertTypes(items)
                .build();
    }

    @Override
    public NotificationSettingsResponse getNotificationSettings(Long userId) {
        // 모든 활성 알림 유형 조회
        List<AlertType> alertTypes = alertTypeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // 사용자 설정 조회
        List<UserNotificationSetting> userSettings = notificationSettingRepository.findByUserIdWithAlertType(userId);

        // 사용자 설정을 alertTypeCode -> isEnabled 맵으로 변환
        Map<String, Boolean> userSettingMap = userSettings.stream()
                .collect(Collectors.toMap(
                        s -> s.getAlertType().getCode(),
                        UserNotificationSetting::getIsEnabled
                ));

        // settingGroup별로 그룹화하고, 그룹 내 모든 alertType이 enabled인지 확인
        Map<String, List<AlertType>> groupedTypes = alertTypes.stream()
                .collect(Collectors.groupingBy(
                        AlertType::getSettingGroup,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<NotificationSettingsResponse.SettingItem> items = new ArrayList<>();
        for (Map.Entry<String, List<AlertType>> entry : groupedTypes.entrySet()) {
            String settingGroup = entry.getKey();
            List<AlertType> types = entry.getValue();

            // 그룹 내 모든 alertType이 enabled인지 확인 (설정 없으면 기본값 true)
            boolean isEnabled = types.stream()
                    .allMatch(at -> userSettingMap.getOrDefault(at.getCode(), true));

            String[] groupInfo = SETTING_GROUP_INFO.getOrDefault(settingGroup, new String[]{settingGroup, ""});

            items.add(NotificationSettingsResponse.SettingItem.builder()
                    .settingGroup(settingGroup)
                    .groupName(groupInfo[0])
                    .description(groupInfo[1])
                    .isEnabled(isEnabled)
                    .build());
        }

        return NotificationSettingsResponse.builder()
                .settings(items)
                .build();
    }

    @Override
    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 요청의 모든 settingGroup 수집
        Set<String> requestedGroups = request.getSettings().stream()
                .map(NotificationSettingsRequest.SettingItem::getSettingGroup)
                .collect(Collectors.toSet());

        List<AlertType> allAlertTypes = alertTypeRepository.findBySettingGroupInAndIsActiveTrue(requestedGroups);

        if (allAlertTypes.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // settingGroup -> alertTypes 맵 생성
        Map<String, List<AlertType>> groupToAlertTypes = allAlertTypes.stream()
                .collect(Collectors.groupingBy(AlertType::getSettingGroup));

        // 모든 alertType 코드 수집
        Set<String> allAlertTypeCodes = allAlertTypes.stream()
                .map(AlertType::getCode)
                .collect(Collectors.toSet());

        List<UserNotificationSetting> existingSettings =
                notificationSettingRepository.findByUserIdAndAlertTypeCodeIn(userId, allAlertTypeCodes);

        Map<String, UserNotificationSetting> existingSettingMap = existingSettings.stream()
                .collect(Collectors.toMap(s -> s.getAlertType().getCode(), s -> s));

        // 설정 업데이트 처리
        List<UserNotificationSetting> newSettings = new ArrayList<>();
        for (NotificationSettingsRequest.SettingItem item : request.getSettings()) {
            List<AlertType> alertTypes = groupToAlertTypes.get(item.getSettingGroup());
            if (alertTypes == null || alertTypes.isEmpty()) {
                continue;
            }

            for (AlertType alertType : alertTypes) {
                UserNotificationSetting setting = existingSettingMap.get(alertType.getCode());

                if (setting != null) {
                    setting.setEnabled(item.getIsEnabled());
                } else {
                    newSettings.add(UserNotificationSetting.builder()
                            .user(user)
                            .alertType(alertType)
                            .isEnabled(item.getIsEnabled())
                            .build());
                }
            }
        }

        // 새 설정 일괄 저장
        if (!newSettings.isEmpty()) {
            notificationSettingRepository.saveAll(newSettings);
        }

        // 수정된 전체 설정 반환
        return getNotificationSettings(userId);
    }
}
