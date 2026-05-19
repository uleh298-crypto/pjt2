package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 회사 정보 Repository
 */
@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {

    /**
     * 회사명으로 검색 (부분 일치)
     */
    List<CompanyInfo> findByCompanyNameContaining(String companyName);

    /**
     * 산업분류 코드로 회사 목록 조회
     */
    List<CompanyInfo> findByIndustry_Code(String industryCode);

    /**
     * 활성화된 회사만 조회
     */
    List<CompanyInfo> findByIsActiveTrue();
}
