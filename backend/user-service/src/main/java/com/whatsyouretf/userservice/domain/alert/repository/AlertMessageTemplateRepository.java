package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.AlertMessageTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 알림 메시지 템플릿 Repository
 */
@Repository
public interface AlertMessageTemplateRepository extends JpaRepository<AlertMessageTemplate, Long> {

    /**
     * 알림 유형 코드로 활성 템플릿 조회 (캐싱)
     */
    @Cacheable(value = "alertTemplate", key = "#alertTypeCode")
    Optional<AlertMessageTemplate> findByAlertTypeCodeAndIsActiveTrue(String alertTypeCode);
}
