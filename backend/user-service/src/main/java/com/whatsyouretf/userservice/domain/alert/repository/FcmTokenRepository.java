package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.DeviceType;
import com.whatsyouretf.userservice.domain.alert.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 Repository
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * 사용자 ID와 기기 유형으로 조회
     */
    Optional<FcmToken> findByUserIdAndDeviceType(Long userId, DeviceType deviceType);

    /**
     * 토큰으로 조회
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * 사용자의 활성 FCM 토큰 목록 조회
     */
    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 사용자 ID와 토큰으로 삭제
     */
    void deleteByUserIdAndToken(Long userId, String token);

    /**
     * 사용자의 모든 FCM 토큰 삭제 (회원 탈퇴 시)
     */
    void deleteAllByUserId(Long userId);
}
