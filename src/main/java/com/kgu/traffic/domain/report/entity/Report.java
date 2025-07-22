package com.kgu.traffic.domain.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String reporterName;
    private String targetName;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime reportedAt;

    @Builder
    public Report(String title, String description, String reporterName, String targetName) {
        this.title = title;
        this.description = description;
        this.reporterName = reporterName;
        this.targetName = targetName;
        this.status = ReportStatus.PENDING;
        this.reportedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}