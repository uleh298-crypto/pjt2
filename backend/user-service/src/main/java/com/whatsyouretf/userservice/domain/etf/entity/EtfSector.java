package com.whatsyouretf.userservice.domain.etf.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EtfSector {
    SEMI("SEMI", "반도체"),
    IT("IT", "IT/전자"),
    BIO("BIO", "바이오/의약"),
    AUTO("AUTO", "자동차"),
    CHEM("CHEM", "화학/소재"),
    ENERGY("ENERGY", "에너지"),
    FINANCE("FINANCE", "금융"),
    CONSTRUCT("CONSTRUCT", "건설/부동산"),
    CONSUMER("CONSUMER", "소비재"),
    TELECOM("TELECOM", "통신/미디어"),
    TRANSPORT("TRANSPORT", "운송/물류"),
    INDUSTRY("INDUSTRY", "산업재"),
    HOLDING("HOLDING", "지주회사"),
    ETC("ETC", "기타");

    private final String tag;
    private final String name;
}
