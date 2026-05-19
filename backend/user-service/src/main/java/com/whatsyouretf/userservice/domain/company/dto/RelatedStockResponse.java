package com.whatsyouretf.userservice.domain.company.dto;

import com.whatsyouretf.userservice.domain.company.entity.Stock;
import lombok.Builder;
import lombok.Getter;

/**
 * 관련 종목 응답 DTO
 */
@Getter
@Builder
public class RelatedStockResponse {

    /** 종목 티커 */
    private String ticker;

    /** 회사명 */
    private String companyName;

    /** 산업분류 코드 */
    private String industryCode;

    /** 산업분류명 */
    private String industryName;

    /** 관계 유형 (동종 업계, 서플라이 체인 등) */
    private String relationType;

    /** 회사 로고 URL */
    private String logoUrl;

    public static RelatedStockResponse from(Stock stock, String industryName, String logoUrl) {
        var industry = stock.getCompany().getIndustry();
        return RelatedStockResponse.builder()
                .ticker(stock.getTicker())
                .companyName(stock.getCompany().getCompanyName())
                .industryCode(industry != null ? industry.getCode() : null)
                .industryName(industryName)
                .relationType("동종 업계")
                .logoUrl(logoUrl)
                .build();
    }
}
