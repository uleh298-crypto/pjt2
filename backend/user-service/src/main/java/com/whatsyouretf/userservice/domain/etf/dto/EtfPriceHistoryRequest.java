package com.whatsyouretf.userservice.domain.etf.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EtfPriceHistoryRequest {
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @AssertTrue(message = "시작일은 종료일보다 앞서야 합니다.")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) return true;
        return !startDate.isAfter(endDate);
    }
}
