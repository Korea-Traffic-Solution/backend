package com.kgu.traffic.domain.report.service;

import com.kgu.traffic.domain.auth.entity.Admin;
import com.kgu.traffic.domain.auth.repository.AdminRepository;
import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.request.ReportCreateRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.dto.response.ReportStatisticsResponse;
import com.kgu.traffic.domain.report.entity.Report;
import com.kgu.traffic.domain.report.repository.ReportRepository;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static com.kgu.traffic.domain.report.entity.ReportStatus.PENDING;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;
    private final FirestoreService firestoreService;

    @Transactional(readOnly = true)
    protected Admin getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) auth.getPrincipal();
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

        return reportRepository.findAllByAddressContaining(normalizedRegion, pageable)
                .map(r -> new ReportSimpleResponse(
                        r.getId(),
                        r.getTitle(),
                        r.getReporterName(),
                        r.getStatus(),
                        r.getReportedAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<ReportSimpleResponse> getReportsByRegion(Pageable pageable) {
        Admin admin = getCurrentAdmin();
        String region = firestoreService.getManagerRegion(admin.getRegion());
        String normalizedRegion = normalizeRegion(region);

        return reportRepository.findAllByAddressContaining(normalizedRegion, pageable)
                .map(r -> new ReportSimpleResponse(
                        r.getId(),
                        r.getTitle(),
                        r.getReporterName(),
                        r.getStatus(),
                        r.getReportedAt()
                ));
    }

    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        Admin admin = getCurrentAdmin();
        String region = firestoreService.getManagerRegion(admin.getRegion());
        String normalizedRegion = normalizeRegion(region);

        if (!report.getAddress().contains(normalizedRegion)) {
            throw TrafficException.from(ErrorCode.FORBIDDEN_ACCESS);
        }

        Map<String, Object> firestoreData = firestoreService.getReportFirestoreData(id);

        return new ReportDetailResponse(
                report.getId(),
                report.getTitle(),
                report.getDescription(),
                report.getReporterName(),
                report.getTargetName(),
                report.getStatus(),
                report.getReportedAt(),
                report.getAddress(),
                report.getGps(),
                report.getReason(),
                report.getFine(),
                report.getBrand(),
                report.getApprovedAt(),

                (String) firestoreData.get("aiResult"),
                (String) firestoreData.get("detectedBrand"),
                (String) firestoreData.get("location"),
                (String) firestoreData.get("reportContent")
        );
    }

    @Transactional
    public void processReport(Long id, ReportApproveRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        Admin admin = getCurrentAdmin();
        String region = firestoreService.getManagerRegion(admin.getRegion());
        String normalizedRegion = normalizeRegion(region);

        if (!report.getAddress().contains(normalizedRegion)) {
            throw TrafficException.from(ErrorCode.FORBIDDEN_ACCESS);
        }

        if (request.approve()) {
            report.approve(request.reason(), request.fine(), admin);

            firestoreService.saveReportToFirestore(id, Map.of(
                    "aiResult", "노뚝",
                    "detectedBrand", report.getBrand(),
                    "location", report.getAddress(),
                    "reportContent", report.getDescription()
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
        Admin admin = getCurrentAdmin();
        String region = firestoreService.getManagerRegion(admin.getRegion());
        String normalizedRegion = normalizeRegion(region);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        return reportRepository.findAllByAddressContainingAndReportedAtBetween(
                normalizedRegion, startOfMonth, endOfMonth, pageable
        ).map(r -> new ReportSimpleResponse(
                r.getId(),
                r.getTitle(),
                r.getReporterName(),
                r.getStatus(),
                r.getReportedAt()
        ));
    }

    @Transactional
    public void createReport(ReportCreateRequest request) {
        Report report = Report.builder()
                .title(request.title())
                .description(request.description())
                .reporterName(request.reporterName())
                .targetName(request.targetName())
                .address(request.address())
                .gps(request.gps())
                .brand(request.brand())
                .status(PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        Report saved = reportRepository.save(report);

        firestoreService.saveReportToFirestore(saved.getId(), Map.of(
                "aiResult", "분석 결과 예시",
                "detectedBrand", saved.getBrand(),
                "location", saved.getAddress(),
                "reportContent", saved.getDescription()
        ));
    }
}