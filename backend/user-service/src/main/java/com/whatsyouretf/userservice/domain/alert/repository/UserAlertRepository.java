package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.AlertCategory;
import com.whatsyouretf.userservice.domain.alert.entity.UserAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 알림 Repository
 */
@Repository
public interface UserAlertRepository extends JpaRepository<UserAlert, Long> {

    /**
     * 사용자 알림 목록 조회 (최신순) - 페이징
     */
    Page<UserAlert> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 알림 목록 조회 (카테고리 필터, 최신순) - 페이징
     */
    @Query("SELECT ua FROM UserAlert ua " +
           "JOIN ua.alertType at " +
           "WHERE ua.user.id = :userId AND at.category = :category " +
           "ORDER BY ua.createdAt DESC")
    Page<UserAlert> findByUserIdAndCategory(@Param("userId") Long userId,
                                            @Param("category") AlertCategory category,
                                            Pageable pageable);

    /**
     * 최근 7일 알림 목록 조회 (최신순)
     */
    @Query("SELECT ua FROM UserAlert ua " +
           "JOIN FETCH ua.alertType at " +
           "WHERE ua.user.id = :userId AND ua.createdAt >= :since " +
           "ORDER BY ua.createdAt DESC")
    List<UserAlert> findRecentByUserId(@Param("userId") Long userId,
                                       @Param("since") LocalDateTime since);

    /**
     * 최근 7일 알림 목록 조회 (카테고리 필터, 최신순)
     */
    @Query("SELECT ua FROM UserAlert ua " +
           "JOIN FETCH ua.alertType at " +
           "WHERE ua.user.id = :userId AND at.category = :category AND ua.createdAt >= :since " +
           "ORDER BY ua.createdAt DESC")
    List<UserAlert> findRecentByUserIdAndCategory(@Param("userId") Long userId,
                                                  @Param("category") AlertCategory category,
                                                  @Param("since") LocalDateTime since);

    /**
     * 사용자 ID와 알림 ID로 조회
     */
    Optional<UserAlert> findByIdAndUserId(Long id, Long userId);

    /**
     * 읽지 않은 알림 수 조회
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE UserAlert ua SET ua.isRead = true, ua.readAt = CURRENT_TIMESTAMP " +
           "WHERE ua.user.id = :userId AND ua.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 읽은 알림 전체 삭제
     */
    @Modifying
    @Query("DELETE FROM UserAlert ua WHERE ua.user.id = :userId AND ua.isRead = true")
    int deleteAllReadByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 알림 전체 삭제
     */
    @Modifying
    @Query("DELETE FROM UserAlert ua WHERE ua.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

