package com.whatsyouretf.userservice.domain.simulation.repository;

import com.whatsyouretf.userservice.domain.simulation.entity.Simulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 시뮬레이션 레포지토리
 */
@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

    /**
     * 사용자의 시뮬레이션 목록 조회 (최신순)
     */
    Page<Simulation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 시뮬레이션 상세 조회 (사용자 검증 포함)
     */
    Optional<Simulation> findByIdAndUserId(Long id, Long userId);

    /**
     * 시뮬레이션 상세 조회 (연관 엔티티 페치 조인)
     */
    @Query("SELECT s FROM Simulation s " +
            "LEFT JOIN FETCH s.portfolio " +
            "LEFT JOIN FETCH s.monthlyReturns " +
            "LEFT JOIN FETCH s.etfPerformances ep " +
            "LEFT JOIN FETCH ep.etf " +
            "WHERE s.id = :id AND s.user.id = :userId")
    Optional<Simulation> findByIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 사용자의 시뮬레이션 개수 조회
     */
    long countByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
