package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 알림 유형 Repository
 */
@Repository
public interface AlertTypeRepository extends JpaRepository<AlertType, String> {

    /**
     * 활성 알림 유형 목록 조회 (정렬순)
     */
    List<AlertType> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 설정 그룹으로 알림 유형 목록 조회
     */
    List<AlertType> findBySettingGroupAndIsActiveTrue(String settingGroup);

    /**
     * 여러 설정 그룹으로 알림 유형 배치 조회
     */
    List<AlertType> findBySettingGroupInAndIsActiveTrue(Collection<String> settingGroups);

    /**
     * 활성 알림 유형의 설정 그룹 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT a.settingGroup FROM AlertType a WHERE a.isActive = true ORDER BY MIN(a.displayOrder)")
    List<String> findDistinctSettingGroups();
}
