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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.kgu.traffic.domain.report.entity.ReportStatus.PENDING;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;
    private final FirestoreService firestoreService;

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
        Admin admin = getCurrentAdmin();
        String region = firestoreService.getManagerRegion(admin.getRegion());
        String normalizedRegion = normalizeRegion(region);

        List<QueryDocumentSnapshot> conclusions = firestoreService.getAllConclusions();

        List<ReportSimpleResponse> reportList = conclusions.stream()
                .filter(doc -> {
                    String docRegion = doc.getString("region");
                    return docRegion != null && docRegion.contains(normalizedRegion);
                })
                .map(doc -> {
                    String title = doc.contains("title") ? doc.getString("title")
                            : String.valueOf(doc.get("violation"));
                    String reporterName = doc.contains("userId") ? doc.getString("userId") : "익명";
                    LocalDateTime reportedAt = LocalDateTime.now(); // Conclusion 원본엔 date가 문자열이므로 여기선 현재시간 대체
                    ReportStatus status = ReportStatus.PENDING;
                    String id = doc.getId();
                    return new ReportSimpleResponse(id, title, reporterName, status, reportedAt);
                })
                .sorted((r1, r2) -> r2.reportedAt().compareTo(r1.reportedAt()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), reportList.size());
        List<ReportSimpleResponse> pageContent = (start >= reportList.size())
                ? List.of() : reportList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, reportList.size());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(String docId) {
        Map<String, Object> fs = firestoreService.getConclusionByDocId(docId);

        List<String> aiConclusion = List.of();
        Object aiObj = fs.get("aiConclusion");
        if (aiObj instanceof List<?> list) {
            aiConclusion = list.stream().map(String::valueOf).toList();
        }

        Double confidence = null;
        Object conf = fs.get("confidence");
        if (conf instanceof Number n) confidence = n.doubleValue();

        return new ReportDetailResponse(
                docId,
                aiConclusion,
                confidence,
                fs.get("date") != null ? String.valueOf(fs.get("date")) : null,
                (String) fs.getOrDefault("detectedBrand", null),
                (String) fs.getOrDefault("gpsInfo", null),
                (String) fs.getOrDefault("imageUrl", null),
                (String) fs.getOrDefault("region", null),
                (String) fs.getOrDefault("reportImgUrl", null),
                (String) fs.getOrDefault("result", null),
                (String) fs.getOrDefault("userId", null),
                (String) fs.getOrDefault("violation", null)
        );
    }

    @Transactional
    public void processReport(String docId, ReportApproveRequest request) {
        var report = reportRepository.findByFirestoreDocId(docId)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        if (request.approve()) {
            report.approve(request.reason(), request.fine(), getCurrentAdmin());
        } else {
            report.reject(request.reason(), getCurrentAdmin());
        }
    }

    @Transactional(readOnly = true)
    public ReportStatisticsResponse getReportStatistics() {
        var admin = getCurrentAdmin();
        var region = firestoreService.getManagerRegion(admin.getRegion());
        var normalizedRegion = normalizeRegion(region);

        var conclusions = firestoreService.getAllConclusions();

        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        long total = 0, monthly = 0, approved = 0, rejected = 0;

        for (var doc : conclusions) {
            String docRegion = doc.getString("region");
            if (docRegion == null || !docRegion.contains(normalizedRegion)) continue;

            total++;

            LocalDateTime reportedAt = parseToLocalDateTime(doc.get("date"));
            if (reportedAt != null &&
                    !reportedAt.isBefore(startOfMonth) &&
                    !reportedAt.isAfter(endOfMonth)) {
                monthly++;
            }

            String result = doc.getString("result");
            if ("승인".equals(result)) approved++;
            else if ("반려".equals(result)) rejected++;
        }

        return new ReportStatisticsResponse(total, monthly, approved, rejected);
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
        reportRepository.save(report);
    }

    private LocalDateTime parseToLocalDateTime(Object dateObj) {
        if (dateObj == null) return null;
        try {
            if (dateObj instanceof com.google.cloud.Timestamp ts) {
                return ts.toSqlTimestamp().toLocalDateTime();
            } else if (dateObj instanceof java.util.Date d) {
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else if (dateObj instanceof String s) {
                String cleaned = s.replace("UTC+9", "").replace("KST", "").trim();
                Locale ko = Locale.KOREAN;
                String[] patterns = {
                        "yyyy년 M월 d일 a h시 m분 s초",
                        "yyyy년 M월 d일 a h시 m분",
                        "yyyy-MM-dd HH:mm:ss",
                        "yyyy-MM-dd'T'HH:mm:ssXXX",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                };
                for (String p : patterns) {
                    try {
                        return LocalDateTime.parse(cleaned, DateTimeFormatter.ofPattern(p, ko));
                    } catch (Exception ignore) {}
                }
                try { // ISO-8601 with offset
                    return java.time.OffsetDateTime.parse(s).toLocalDateTime();
                } catch (Exception ignore) {}
                try {
                    long millis = Long.parseLong(s.trim());
                    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}
        return null;
    }
}