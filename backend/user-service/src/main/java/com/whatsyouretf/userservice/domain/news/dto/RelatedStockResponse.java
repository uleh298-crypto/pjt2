package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 관련 종목 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedStockResponse {

    /** 회사 ID */
    private Long companyId;

    /** 종목코드 (티커) */
    private String ticker;

    /** 종목명 */
    private String name;

    /** 산업 그룹 */
    private String industryGroup;

    /** ETF 내 비중 (ETF 뉴스 조회 시) */
    private BigDecimal weightPct;

    /**
     * CompanyInfo -> DTO 변환 (ticker 없음)
     */
    public static RelatedStockResponse from(CompanyInfo company) {
        return RelatedStockResponse.builder()
                .companyId(company.getId())
                .name(company.getCompanyName())
                .industryGroup(company.getIndustryGroup())
                .build();
    }

    /**
     * CompanyInfo + ticker -> DTO 변환
     */
    public static RelatedStockResponse from(CompanyInfo company, String ticker) {
        return RelatedStockResponse.builder()
                .companyId(company.getId())
                .ticker(ticker)
                .name(company.getCompanyName())
                .industryGroup(company.getIndustryGroup())
                .build();
    }

    /**
     * CompanyInfo + ticker + 비중 -> DTO 변환
     */
    public static RelatedStockResponse from(CompanyInfo company, String ticker, BigDecimal weightPct) {
        return RelatedStockResponse.builder()
                .companyId(company.getId())
                .ticker(ticker)
                .name(company.getCompanyName())
                .industryGroup(company.getIndustryGroup())
                .weightPct(weightPct)
                .build();
    }
}
