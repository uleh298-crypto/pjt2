package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByEmailAndToken(String email, String token);

    @Query("SELECT t FROM EmailVerificationToken t WHERE t.email = :email AND t.isVerified = false ORDER BY t.createdAt DESC LIMIT 1")
    Optional<EmailVerificationToken> findLatestByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    boolean existsByEmailAndIsVerifiedFalse(String email);

    /**
     * 인증 완료된 토큰 조회 (회원가입 완료 시 사용)
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.email = :email AND t.isVerified = true ORDER BY t.createdAt DESC LIMIT 1")
    Optional<EmailVerificationToken> findVerifiedByEmail(@Param("email") String email);
}
