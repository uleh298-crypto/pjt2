package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.etf.dto.EtfCurrentInfo;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 관련 ETF 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedEtfResponse {

    /** ETF ID */
    private Long etfId;

    /** ETF 종목코드 */
    private String ticker;

    /** ETF 명칭 */
    private String name;

    /** 자산운용사 */
    private String manager;

    /** 등락률 (%) */
    private BigDecimal changeRate;

    /**
     * Entity + CurrentInfo (Redis 캐시) -> DTO 변환
     */
    public static RelatedEtfResponse from(Etf etf, EtfCurrentInfo currentInfo) {
        return RelatedEtfResponse.builder()
                .etfId(etf.getId())
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .manager(etf.getAssetManager())
                .changeRate(currentInfo != null ? currentInfo.dailyReturn() : BigDecimal.ZERO)
                .build();
    }

    /**
     * Entity -> DTO 변환 (시세 없이)
     */
    public static RelatedEtfResponse from(Etf etf) {
        return from(etf, null);
    }
}
