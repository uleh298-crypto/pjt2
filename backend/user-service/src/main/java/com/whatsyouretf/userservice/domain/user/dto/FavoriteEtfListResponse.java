package com.whatsyouretf.userservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 관심 ETF 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteEtfListResponse {

    /** 관심 ETF 목록 */
    private List<FavoriteEtfResponse> favorites;

    /** 총 개수 */
    private int totalCount;

    public static FavoriteEtfListResponse of(List<FavoriteEtfResponse> favorites) {
        return FavoriteEtfListResponse.builder()
                .favorites(favorites)
                .totalCount(favorites.size())
                .build();
    }
}
