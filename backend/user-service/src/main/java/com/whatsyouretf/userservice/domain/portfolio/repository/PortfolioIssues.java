package com.whatsyouretf.userservice.domain.portfolio.repository;

import java.time.LocalDate;

public record PortfolioIssues(
    LocalDate localDate,
    String title,
    String description
) {
}
