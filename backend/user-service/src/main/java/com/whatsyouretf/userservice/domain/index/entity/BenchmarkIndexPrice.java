package com.whatsyouretf.userservice.domain.index.entity;

import com.whatsyouretf.userservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "benchmark_index_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BenchmarkIndexPrice extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "close")
        private BigDecimal close;

        @Enumerated(value = EnumType.STRING)
        private MarketType marketType;

        @Column(name = "trading_date")
        private LocalDate tradingDate;
}
