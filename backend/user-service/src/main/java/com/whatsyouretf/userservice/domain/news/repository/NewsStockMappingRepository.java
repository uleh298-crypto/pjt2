package com.whatsyouretf.userservice.domain.news.repository;

import com.whatsyouretf.userservice.domain.news.entity.NewsStockMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 뉴스-종목 매핑 Repository
 */
@Repository
public interface NewsStockMappingRepository extends JpaRepository<NewsStockMapping, Long> {

    /**
     * 뉴스 ID로 매핑 목록 조회
     */
    List<NewsStockMapping> findByNewsArticleId(Long newsId);

    /**
     * 회사 ID로 매핑 목록 조회
     */
    List<NewsStockMapping> findByCompanyInfoId(Long companyId);

    /**
     * 뉴스-종목 매핑 존재 여부 확인
     */
    boolean existsByNewsArticleIdAndCompanyInfoId(Long newsId, Long companyId);

    /**
     * ETF와 관련된 뉴스 매핑 조회 (구성종목 비중 포함)
     */
    @Query("SELECT nsm, ec.weightPct FROM NewsStockMapping nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND nsm.newsArticle.isActive = true " +
           "ORDER BY ec.weightPct DESC, nsm.newsArticle.publishedAt DESC")
    List<Object[]> findByEtfIdWithWeight(@Param("etfId") Long etfId);
}
