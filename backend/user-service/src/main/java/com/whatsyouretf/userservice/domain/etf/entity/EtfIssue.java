package com.whatsyouretf.userservice.domain.etf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "etf_issue", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"etf_id", "issue_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;
}
