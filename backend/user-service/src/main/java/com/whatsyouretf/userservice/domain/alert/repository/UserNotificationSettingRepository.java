package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 알림 설정 Repository
 */
@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    /**
     * 사용자의 알림 설정 목록 조회
     */
    @Query("SELECT uns FROM UserNotificationSetting uns " +
           "JOIN FETCH uns.alertType at " +
           "WHERE uns.user.id = :userId " +
           "ORDER BY at.displayOrder ASC")
    List<UserNotificationSetting> findByUserIdWithAlertType(@Param("userId") Long userId);

    /**
     * 사용자 ID와 알림 유형 코드로 조회
     */
    Optional<UserNotificationSetting> findByUserIdAndAlertTypeCode(Long userId, String alertTypeCode);

    /**
     * 사용자 ID와 여러 알림 유형 코드로 배치 조회
     */
    @Query("SELECT uns FROM UserNotificationSetting uns " +
           "JOIN FETCH uns.alertType at " +
           "WHERE uns.user.id = :userId AND at.code IN :alertTypeCodes")
    List<UserNotificationSetting> findByUserIdAndAlertTypeCodeIn(
            @Param("userId") Long userId,
            @Param("alertTypeCodes") Collection<String> alertTypeCodes);

    /**
     * 사용자의 알림 설정 전체 삭제
     */
    void deleteAllByUserId(Long userId);
}
