package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.UserHoldingEtf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 보유 ETF Repository (마이데이터 연동)
 */
@Repository
public interface UserHoldingEtfRepository extends JpaRepository<UserHoldingEtf, Long> {

    /**
     * 사용자의 보유 ETF 목록 조회 (ETF 정보 포함)
     */
    @Query("SELECT uh FROM UserHoldingEtf uh JOIN FETCH uh.etf WHERE uh.user.id = :userId ORDER BY uh.createdAt DESC")
    List<UserHoldingEtf> findAllByUserIdWithEtf(@Param("userId") Long userId);

    /**
     * 사용자 + ETF로 보유 ETF 조회
     */
    @Query("SELECT uh FROM UserHoldingEtf uh WHERE uh.user.id = :userId AND uh.etf.id = :etfId")
    Optional<UserHoldingEtf> findByUserIdAndEtfId(@Param("userId") Long userId, @Param("etfId") Long etfId);

    /**
     * 사용자의 보유 ETF 전체 삭제 (재동기화 시 사용)
     */
    void deleteAllByUserId(Long userId);

    /**
     * 사용자의 보유 ETF 개수 조회
     */
    long countByUserId(Long userId);
}
