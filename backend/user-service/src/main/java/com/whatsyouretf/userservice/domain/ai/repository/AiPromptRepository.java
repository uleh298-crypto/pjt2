package com.whatsyouretf.userservice.domain.ai.repository;

import com.whatsyouretf.userservice.domain.ai.entity.AiPrompt;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI 프롬프트 Repository
 */
@Repository
public interface AiPromptRepository extends JpaRepository<AiPrompt, Long> {

    /**
     * 이름으로 활성 프롬프트 조회 (캐싱 적용)
     * - 캐시 TTL: 24시간 (CacheConfig 참고)
     */
    @Cacheable(value = "aiPrompts", key = "#name")
    Optional<AiPrompt> findByNameAndIsActiveTrue(String name);
}
