package com.whatsyouretf.userservice.domain.common.repository;

import com.whatsyouretf.userservice.domain.common.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 카테고리 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    /**
     * 타입별 활성 카테고리 목록 조회
     */
    List<Category> findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(String type);

    /**
     * 타입별 카테고리 목록 조회
     */
    List<Category> findByTypeOrderByDisplayOrderAsc(String type);
}
