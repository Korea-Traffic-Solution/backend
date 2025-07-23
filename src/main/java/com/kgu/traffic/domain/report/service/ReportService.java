package com.kgu.traffic.domain.report.service;

import com.kgu.traffic.domain.auth.entity.Admin;
import com.kgu.traffic.domain.auth.repository.AdminRepository;
import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.dto.response.ReportStatisticsResponse;
import com.kgu.traffic.domain.report.entity.Report;
import com.kgu.traffic.domain.report.repository.ReportRepository;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final AdminRepository adminRepository;


    @Transactional(readOnly = true)
    public Page<ReportSimpleResponse> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
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
                report.getApprovedAt()
        );
    }

    @Transactional
    public void processReport(Long id, ReportApproveRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        Admin admin = getCurrentAdmin(); // 인증된 관리자 가져오는 메서드

        if (request.approve()) {
            report.approve(request.reason(), request.fine(), admin);
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

    private Admin getCurrentAdmin() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> TrafficException.from(ErrorCode.ADMIN_NOT_FOUND));
    }
}