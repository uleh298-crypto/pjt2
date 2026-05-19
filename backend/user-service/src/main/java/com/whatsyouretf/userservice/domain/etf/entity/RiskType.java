package com.whatsyouretf.userservice.domain.etf.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RiskType {
    CONSERVATIVE(5, "안정형"),
    STABLE(4, "안정추구형"),
    MODERATE(3, "위험중립형"),
    ACTIVE(2,"적극투자형"),
    AGGRESSIVE(1, "공격투자형");

    private final Integer riskGrade;
    private final String typeName;
}
