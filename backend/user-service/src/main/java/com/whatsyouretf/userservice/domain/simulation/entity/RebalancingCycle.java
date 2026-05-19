package com.whatsyouretf.userservice.domain.simulation.entity;

/**
 * 리밸런싱 주기 열거형
 */
public enum RebalancingCycle {
    NONE,       // 리밸런싱 없음
    MONTHLY,    // 월간
    QUARTERLY,  // 분기
    YEARLY      // 연간
}
