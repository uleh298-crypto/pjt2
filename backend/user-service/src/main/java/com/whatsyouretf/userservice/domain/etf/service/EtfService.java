package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.dto.EtfClusterResponse;
import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ETF 서비스 인터페이스
 */
public interface EtfService {

    /**
     * ETF 상세 조회 (클러스터 뷰 포함)
     *
     * @param ticker ETF 종목코드
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이지 정보
     * @return ETF 상세 정보
     */
    Page<EtfPrice> getEtfHistory(String ticker, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Etf getEtfDetail(String ticker);

    EtfCurrentInfo getEtfCurrentInfo(String ticker);

    Page<EtfSummary> getEtfList(EtfQuery query, Pageable pageable);

    EtfClusterResponse getClusterData(String ticker);

    Map<String, Etf> getEtfListInTickers(List<String> list);

    Map<String, EtfCurrentInfo> getEtfCurrentInfoMap(Set<String> tickers);

    List<EtfStockComposition> getEtfsIncludingStock(String ticker);

    List<EtfCurrentInfo> getTopTenList();

    List<EtfDividendsData> getEtfDividends(String ticker);
}
