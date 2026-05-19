package com.whatsyouretf.userservice.domain.common.repository;

import com.whatsyouretf.userservice.domain.common.entity.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 데이터 소스 Repository
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {

    /**
     * 활성 데이터 소스 목록 조회
     */
    List<DataSource> findByIsActiveTrue();
}
