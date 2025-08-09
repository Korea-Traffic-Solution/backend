package com.kgu.traffic.domain.report.controller;

import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.request.ReportCreateRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.dto.response.ReportStatisticsResponse;
import com.kgu.traffic.domain.report.service.ReportService;
import com.kgu.traffic.global.domain.SuccessCode;
import com.kgu.traffic.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "ReportController", description = "신고 관리 API")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "신고 목록 조회", description = "페이징 처리된 신고 목록을 반환합니다.")
    public ApiResponse<ReportSimpleResponse> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedAt").descending());
        Page<ReportSimpleResponse> reports = reportService.getReports(pageable);
        return new ApiResponse<>(reports);
    }

    @GetMapping("/{id}")
    @Operation(summary = "신고 상세 조회(Conclusion 원본)", description = "Conclusion 컬렉션 문서를 변환 없이 그대로 반환합니다.")
    public ApiResponse<ReportDetailResponse> getReportDetail(@PathVariable String id) {
        return new ApiResponse<>(reportService.getReportDetail(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "신고 승인/반려 처리", description = "신고를 승인 또는 반려 처리합니다.")
    public ApiResponse<Void> processReport(
            @PathVariable String id,
            @RequestBody @Valid ReportApproveRequest request
    ) {
        reportService.processReport(id, request);
        return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }

    @GetMapping("/monthly")
    @Operation(summary = "이번 달 신고 목록 조회", description = "이번 달에 생성된 페이징된 신고 목록을 반환합니다.")
    public ApiResponse<ReportSimpleResponse> getMonthlyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedAt").descending());
        Page<ReportSimpleResponse> reports = reportService.getReports(pageable);
        return new ApiResponse<>(reports);
    }
}