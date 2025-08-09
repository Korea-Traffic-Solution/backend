package com.kgu.traffic.domain.report.entity;

import com.kgu.traffic.domain.auth.entity.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    private String imageUrl;

    @Column(name = "firestore_doc_id")
    private String firestoreDocId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public void linkFirestoreDoc(String docId) {
        this.firestoreDocId = docId;
    }

    public void approve(String reason, Integer fine, Admin admin) {
        this.status = ReportStatus.APPROVED;
        this.reason = reason;
        this.fine = fine;
        this.approvedAt = LocalDateTime.now();
        this.admin = admin;
    }

    public void reject(String reason, Admin admin) {
        this.status = ReportStatus.REJECTED;
        this.reason = reason;
        this.admin = admin;
    }
}