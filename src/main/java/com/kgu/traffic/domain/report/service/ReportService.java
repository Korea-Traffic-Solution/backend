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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        List<QueryDocumentSnapshot> filtered = conclusions.stream()
                .filter(doc -> {
                    String docRegion = doc.getString("region");
                    return docRegion != null && docRegion.contains(normalizedRegion);
                })
                .toList();

        List<String> ids = filtered.stream().map(QueryDocumentSnapshot::getId).toList();
        Map<String, ReportStatus> statusMap = reportRepository.findByFirestoreDocIdIn(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Report::getFirestoreDocId,
                        Report::getStatus
                ));

        List<ReportSimpleResponse> reportList = filtered.stream()
                .map(doc -> {
                    String title = doc.contains("title") ? doc.getString("title")
                            : String.valueOf(doc.get("violation"));
                    String reporterName = doc.contains("userId") ? doc.getString("userId") : "익명";
                    LocalDateTime reportedAt = parseToLocalDateTime(doc.get("date"));
                    if (reportedAt == null) reportedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                    String id = doc.getId();

                    ReportStatus status = statusMap.getOrDefault(id, ReportStatus.PENDING);

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

        String imageUrlRaw = (String) fs.get("imageUrl");
        String reportImgUrlRaw = (String) fs.get("reportImgUrl");

        String token = extractTokenFromUrl(imageUrlRaw);
        if (token == null) token = extractTokenFromUrl(reportImgUrlRaw);

        String fixedImageUrl = toFirebaseDownloadUrl(imageUrlRaw, token);
        String fixedReportImgUrl = toFirebaseDownloadUrl(reportImgUrlRaw, token);

        return new ReportDetailResponse(
                docId,
                aiConclusion,
                confidence,
                fs.get("date") != null ? String.valueOf(fs.get("date")) : null,
                (String) fs.getOrDefault("detectedBrand", null),
                (String) fs.getOrDefault("gpsInfo", null),
                fixedImageUrl,
                (String) fs.getOrDefault("region", null),
                fixedReportImgUrl,
                (String) fs.getOrDefault("result", null),
                (String) fs.getOrDefault("userId", null),
                (String) fs.getOrDefault("violation", null)
        );
    }

    @Transactional
    public void processReport(String docId, ReportApproveRequest request) {
        Report report = reportRepository.findByFirestoreDocId(docId)
                .orElseGet(() -> upsertReportFromFirestore(docId));

        if (request.approve()) {
            report.approve(request.reason(), request.fine(), getCurrentAdmin());
        } else {
            report.reject(request.reason(), getCurrentAdmin());
        }
    }

    private Report upsertReportFromFirestore(String docId) {
        Map<String, Object> fs = firestoreService.getConclusionByDocId(docId);
        if (fs == null || fs.isEmpty()) {
            throw TrafficException.from(ErrorCode.REPORT_NOT_FOUND);
        }

        String title = fs.containsKey("title") ? String.valueOf(fs.get("title"))
                : String.valueOf(fs.getOrDefault("violation", "제목 없음"));
        String reporterName = String.valueOf(fs.getOrDefault("userId", "익명"));
        String targetName = null;
        String address = String.valueOf(fs.getOrDefault("region", ""));
        String gps = String.valueOf(fs.getOrDefault("gpsInfo", ""));
        String brand = String.valueOf(fs.getOrDefault("detectedBrand", ""));
        String imageUrl = (String) fs.getOrDefault("imageUrl", fs.getOrDefault("reportImgUrl", null));

        LocalDateTime reportedAt = parseToLocalDateTime(fs.get("date"));
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }

        Report newReport = Report.builder()
                .title(title)
                .description(null)
                .reporterName(reporterName)
                .targetName(targetName)
                .status(ReportStatus.PENDING)
                .reportedAt(reportedAt)
                .address(address)
                .gps(gps)
                .reason(null)
                .fine(0)
                .brand(brand)
                .approvedAt(null)
                .imageUrl(imageUrl)
                .build();
        newReport.linkFirestoreDoc(docId);

        return reportRepository.save(newReport);
    }

    @Transactional(readOnly = true)
    public ReportStatisticsResponse getReportStatistics() {
        var admin = getCurrentAdmin();
        var region = firestoreService.getManagerRegion(admin.getRegion());
        var normalizedRegion = normalizeRegion(region);

        var conclusions = firestoreService.getAllConclusions();

        var filtered = conclusions.stream()
                .filter(doc -> {
                    String docRegion = doc.getString("region");
                    return docRegion != null && docRegion.contains(normalizedRegion);
                })
                .toList();

        var ids = filtered.stream().map(com.google.cloud.firestore.QueryDocumentSnapshot::getId).toList();
        var statusMap = reportRepository.findByFirestoreDocIdIn(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Report::getFirestoreDocId,
                        Report::getStatus
                ));

        java.time.ZoneId KST = java.time.ZoneId.of("Asia/Seoul");
        java.time.LocalDateTime now = java.time.LocalDateTime.now(KST);
        var startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        var endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        long total = 0, monthly = 0, approved = 0, rejected = 0;

        for (var doc : filtered) {
            total++;

            var reportedAt = parseToLocalDateTime(doc.get("date"));
            if (reportedAt != null &&
                    !reportedAt.isBefore(startOfMonth) &&
                    !reportedAt.isAfter(endOfMonth)) {
                monthly++;
            }

            var id = doc.getId();
            var st = statusMap.getOrDefault(id, ReportStatus.PENDING);
            if (st == ReportStatus.APPROVED) approved++;
            else if (st == ReportStatus.REJECTED) rejected++;
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

    private String extractTokenFromUrl(String url) {
        if (url == null) return null;
        int idx = url.indexOf("token=");
        if (idx < 0) return null;
        int start = idx + "token=".length();
        int end = url.indexOf('&', start);
        return (end > start) ? url.substring(start, end) : url.substring(start);
    }

    private String toFirebaseDownloadUrl(String url, String token) {
        if (url == null || url.isBlank()) return url;

        // 이미 firebasestorage 형식이면 alt=media / token만 보정
        if (url.contains("firebasestorage.googleapis.com")) {
            String u = url;
            if (!u.contains("alt=media")) {
                u += (u.contains("?") ? "&" : "?") + "alt=media";
            }
            if (token != null && !token.isBlank() && !u.contains("token=")) {
                u += (u.contains("?") ? "&" : "?") + "token=" + token;
            }
            return u;
        }

        Matcher m = Pattern.compile("^https?://storage\\.googleapis\\.com/([^/]+)/(.+)$").matcher(url);
        if (m.find()) {
            String bucket = m.group(1);
            String objectPath = m.group(2);
            String encoded = URLEncoder.encode(objectPath, StandardCharsets.UTF_8).replace("+", "%20");
            String base = "https://firebasestorage.googleapis.com/v0/b/" + bucket + "/o/" + encoded + "?alt=media";
            if (token != null && !token.isBlank()) {
                return base + "&token=" + token;
            }
            return base;
        }

        return url;
    }
}