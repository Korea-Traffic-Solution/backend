package com.kgu.traffic.domain.report.service;

import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.entity.Report;
import com.kgu.traffic.domain.report.repository.ReportRepository;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

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
                report.getReportedAt()
        );
    }

    @Transactional
    public void processReport(Long id, ReportApproveRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> TrafficException.from(ErrorCode.REPORT_NOT_FOUND));

        if (request.approve()) {
            report.approve();
        } else {
            report.reject();
        }
    }
}