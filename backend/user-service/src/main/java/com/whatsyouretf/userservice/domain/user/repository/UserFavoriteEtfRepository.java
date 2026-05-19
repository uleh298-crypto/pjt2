package com.whatsyouretf.userservice.domain.user.repository;

import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 관심 ETF Repository
 */
@Repository
public interface UserFavoriteEtfRepository extends JpaRepository<UserFavoriteEtf, Long> {

    /**
     * 사용자의 관심 ETF 목록 조회 (ETF 정보 포함)
     */
    @Query("SELECT uf FROM UserFavoriteEtf uf JOIN FETCH uf.etf WHERE uf.user.id = :userId ORDER BY uf.createdAt DESC")
    List<UserFavoriteEtf> findAllByUserIdWithEtf(@Param("userId") Long userId);

    /**
     * 사용자 + ETF로 관심 ETF 조회
     */
    @Query("SELECT uf FROM UserFavoriteEtf uf WHERE uf.user.id = :userId AND uf.etf.id = :etfId")
    Optional<UserFavoriteEtf> findByUserIdAndEtfId(@Param("userId") Long userId, @Param("etfId") Long etfId);

    /**
     * 사용자 + ETF 종목코드로 관심 ETF 조회
     */
    @Query("SELECT uf FROM UserFavoriteEtf uf WHERE uf.user.id = :userId AND uf.etf.stockCode = :ticker")
    Optional<UserFavoriteEtf> findByUserIdAndTicker(@Param("userId") Long userId, @Param("ticker") String ticker);

    /**
     * 사용자 + ETF 관심 등록 여부 확인
     */
    boolean existsByUserIdAndEtfId(Long userId, Long etfId);

    /**
     * 사용자 + ETF 종목코드 관심 등록 여부 확인
     */
    @Query("SELECT COUNT(uf) > 0 FROM UserFavoriteEtf uf WHERE uf.user.id = :userId AND uf.etf.stockCode = :ticker")
    boolean existsByUserIdAndTicker(@Param("userId") Long userId, @Param("ticker") String ticker);

    /**
     * 사용자의 관심 ETF 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 사용자의 관심 ETF 전체 삭제
     */
    void deleteAllByUserId(Long userId);

    /**
     * 사용자 ID로 관심 ETF 목록 조회
     */
    List<UserFavoriteEtf> findByUserId(Long userId);

    /**
     * 특정 ETF를 관심 등록한 사용자 ID 목록 조회
     */
    @Query("SELECT uf.user.id FROM UserFavoriteEtf uf WHERE uf.etf.id = :etfId")
    List<Long> findUserIdsByEtfId(@Param("etfId") Long etfId);

    /**
     * 특정 ETF를 관심 등록한 사용자 목록 조회 (User 포함)
     */
    @Query("SELECT uf FROM UserFavoriteEtf uf JOIN FETCH uf.user WHERE uf.etf.id = :etfId")
    List<UserFavoriteEtf> findAllByEtfIdWithUser(@Param("etfId") Long etfId);
}
