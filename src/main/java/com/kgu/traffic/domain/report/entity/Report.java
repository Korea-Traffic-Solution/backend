package com.kgu.traffic.domain.report.entity;

import com.kgu.traffic.domain.auth.entity.Admin;
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

    private String address;
    private String gps;
    private String reason;
    private Integer fine;
    private String brand;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Builder
    public Report(String title, String description, String reporterName, String targetName, String address, String gps, String brand) {
        this.title = title;
        this.description = description;
        this.reporterName = reporterName;
        this.targetName = targetName;
        this.address = address;
        this.gps = gps;
        this.brand = brand;
        this.status = ReportStatus.PENDING;
        this.reportedAt = LocalDateTime.now();
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}