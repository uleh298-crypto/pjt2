package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t FROM PasswordResetToken t JOIN FETCH t.user WHERE t.token = :token AND t.isUsed = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.user.id = :userId AND t.isUsed = false")
    void invalidateAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    void deleteAllByUserId(Long userId);
}
