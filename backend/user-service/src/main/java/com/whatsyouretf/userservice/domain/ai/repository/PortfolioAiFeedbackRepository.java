package com.whatsyouretf.userservice.domain.ai.repository;

import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 포트폴리오 AI 피드백 Repository
 */
@Repository
public interface PortfolioAiFeedbackRepository extends JpaRepository<PortfolioAiFeedback, Long> {

    /**
     * 사용자 ID와 리뷰 ID로 조회
     */
    Optional<PortfolioAiFeedback> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 리뷰 히스토리 조회 (최신순)
     */
    Page<PortfolioAiFeedback> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);
}
