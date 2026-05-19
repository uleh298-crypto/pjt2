package com.whatsyouretf.userservice.domain.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업데이트/삭제 카운트 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCountResponse {

    /** 업데이트된 항목 수 */
    private int updatedCount;

    /** 삭제된 항목 수 */
    private int deletedCount;

    public static UpdateCountResponse updated(int count) {
        return UpdateCountResponse.builder().updatedCount(count).build();
    }

    public static UpdateCountResponse deleted(int count) {
        return UpdateCountResponse.builder().deletedCount(count).build();
    }
}
