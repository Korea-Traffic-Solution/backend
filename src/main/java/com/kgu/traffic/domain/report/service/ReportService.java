package com.kgu.traffic.domain.report.service;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.kgu.traffic.domain.auth.entity.Admin;
import com.kgu.traffic.domain.auth.repository.AdminRepository;
import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.request.ReportCreateRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.dto.response.ReportStatisticsResponse;
import com.kgu.traffic.domain.report.entity.Report;
import com.kgu.traffic.domain.report.entity.ReportStatus;
import com.kgu.traffic.domain.report.repository.ReportRepository;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.kgu.traffic.domain.report.entity.ReportStatus.PENDING;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;
    private final FirestoreService firestoreService;
    private final SignedUrlService signedUrlService;

    @Transactional(readOnly = true)
    protected Admin getCurrentAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var admin = (Admin) auth.getPrincipal();
        return adminRepository.findByLoginId(admin.getLoginId())
                .orElseThrow(() -> TrafficException.from(ErrorCode.ADMIN_NOT_FOUND));
    }

    private String normalizeRegion(String region) {
        return region.replaceAll("^(\\p{IsHangul}+)(\\p{IsHangul}{2,3})경찰서$", "$2구");
    }

    @Transactional(readOnly = true)
    public Page<ReportSimpleResponse> getReports(Pageable pageable) {
        List<QueryDocumentSnapshot> conclusions = firestoreService.getAllConclusions();

        List<ReportSimpleResponse> reportList = conclusions.stream()
                .map(doc -> {
                    String title = doc.contains("title") ? doc.getString("title") : "제목 없음";
                    String reporterName = doc.contains("reporterId") ? doc.getString("reporterId") : "익명";
                    com.google.cloud.Timestamp ts = doc.getTimestamp("date");
                    LocalDateTime reportedAt = (ts != null)
                            ? ts.toSqlTimestamp().toLocalDateTime()
                            : LocalDateTime.now();
                    ReportStatus status = ReportStatus.PENDING;
                    String id = doc.getId();

                    return new ReportSimpleResponse(id, title, reporterName, status, reportedAt);
                })
                .sorted((r1, r2) -> r2.reportedAt().compareTo(r1.reportedAt()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reportList.size());
        List<ReportSimpleResponse> pageContent = (start > reportList.size())
                ? List.of()
                : reportList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, reportList.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(String docId) {
        Map<String, Object> fs = firestoreService.getConclusionByDocId(docId);

        Report report = reportRepository.findByFirestoreDocId(docId).orElse(Report.builder().build());

        String title = (String) fs.getOrDefault("title", report.getTitle());
        String description = (String) fs.getOrDefault("description", report.getDescription());
        String reporterName = (String) fs.getOrDefault("userId", report.getReporterName()); // Firestore 필드명 'userId'
        String targetName = (String) fs.getOrDefault("targetName", report.getTargetName());

        com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) fs.get("date");
        LocalDateTime reportedAt = (ts != null) ? ts.toSqlTimestamp().toLocalDateTime() : report.getReportedAt();

        String address = (String) fs.getOrDefault("region", report.getAddress());
        String gps = (String) fs.getOrDefault("gpsInfo", report.getGps());
        String brand = (String) fs.getOrDefault("detectedBrand", report.getBrand());
        List<String> aiConclusion = (List<String>) fs.getOrDefault("aiConclusion", List.of());
        String reportContent = (String) fs.getOrDefault("violation", report.getDescription());

        ReportStatus status;
        String result = (String) fs.get("result");
        if ("미확인".equals(result)) {
            status = ReportStatus.PENDING;
        } else {
            status = report.getStatus();
        }

        String candidateUrl = (String) fs.getOrDefault("imageUrl", (String) fs.get("reportImgUrl"));
        String signedImgUrl = null;
        if (candidateUrl != null && !candidateUrl.isBlank()) {
            signedImgUrl = signedUrlService.toSignedUrl(candidateUrl, Duration.ofMinutes(10));
        }

        return new ReportDetailResponse(
                docId,
                title,
                description,
                reporterName,
                targetName,
                status,
                reportedAt,
                address,
                gps,
                report.getReason(),
                report.getFine(),
                brand,
                report.getApprovedAt(),
                aiConclusion,
                brand,
                address,
                reportContent,
                signedImgUrl
        );
    }

    @Transactional
    public void processReport(String docId, ReportApproveRequest request) {
        var report = reportRepository.findByFirestoreDocId(docId)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        var admin = getCurrentAdmin();
        var region = firestoreService.getManagerRegion(admin.getRegion());
        var normalizedRegion = normalizeRegion(region);

        if (report.getAddress() == null || !report.getAddress().contains(normalizedRegion)) {
            throw TrafficException.from(ErrorCode.FORBIDDEN_ACCESS);
        }

        if (request.approve()) {
            report.approve(request.reason(), request.fine(), admin);
            firestoreService.saveReportToReports(report.getId(), Map.of(
                    "detectedBrand", report.getBrand(),
                    "region", report.getAddress(),
                    "violation", report.getDescription(),
                    "reportImgUrl", report.getImageUrl()
            ));
        } else {
            report.reject(request.reason(), admin);
        }
    }

    @Transactional(readOnly = true)
    public ReportStatisticsResponse getReportStatistics() {
        long total = reportRepository.countAll();
        long monthly = reportRepository.countThisMonth();
        long approved = reportRepository.countApproved();
        long rejected = reportRepository.countRejected();
        return new ReportStatisticsResponse(total, monthly, approved, rejected);
    }

    @Transactional(readOnly = true)
    public Page<ReportSimpleResponse> getMonthlyReportsByRegion(Pageable pageable) {
        var admin = getCurrentAdmin();
        var region = firestoreService.getManagerRegion(admin.getRegion());
        var normalizedRegion = normalizeRegion(region);

        var startOfMonth = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        var endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        return reportRepository.findAllByAddressContainingAndReportedAtBetween(
                normalizedRegion, startOfMonth, endOfMonth, pageable
        ).map(r -> new ReportSimpleResponse(
                r.getId().toString(),
                r.getTitle(),
                r.getReporterName(),
                r.getStatus(),
                r.getReportedAt()
        ));
    }

    @Transactional
    public void createReport(ReportCreateRequest request) {
        var report = Report.builder()
                .title(request.title())
                .description(request.description())
                .reporterName(request.reporterName())
                .targetName(request.targetName())
                .address(request.address())
                .gps(request.gps())
                .brand(request.brand())
                .imageUrl(request.imageUrl())
                .status(PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        var saved = reportRepository.save(report);

        firestoreService.saveReportToReports(saved.getId(), Map.of(
                "detectedBrand", saved.getBrand(),
                "region", saved.getAddress(),
                "violation", saved.getDescription(),
                "reportImgUrl", saved.getImageUrl()
        ));
    }
}