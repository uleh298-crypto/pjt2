package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fundamental {
    private Double per;

    private Double pbr;

    private Double roe;

    public static Fundamental calculateFundamental(
        Double per,
        Double pbr
    ) {
        Fundamental fundamental = new Fundamental();
        fundamental.per = per;
        fundamental.pbr = pbr;
        fundamental.roe = per / pbr;
        return fundamental;
    }
}
