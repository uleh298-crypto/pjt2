package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.IndustryClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 산업 분류 Repository
 */
@Repository
public interface IndustryClassificationRepository extends JpaRepository<IndustryClassification, String> {

    /**
     * 코드로 산업 분류 조회
     */
    Optional<IndustryClassification> findByCode(String code);

    /**
     * 코드 목록으로 산업 분류 배치 조회
     */
    List<IndustryClassification> findByCodeIn(Collection<String> codes);
}
