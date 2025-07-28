package com.kgu.traffic.domain.report.controller;

import com.kgu.traffic.domain.report.dto.request.ReportApproveRequest;
import com.kgu.traffic.domain.report.dto.request.ReportCreateRequest;
import com.kgu.traffic.domain.report.dto.response.ReportDetailResponse;
import com.kgu.traffic.domain.report.dto.response.ReportSimpleResponse;
import com.kgu.traffic.domain.report.dto.response.ReportStatisticsResponse;
import com.kgu.traffic.domain.report.service.ReportExcelService;
import com.kgu.traffic.domain.report.service.ReportService;
import com.kgu.traffic.global.dto.response.ApiResponse;
import com.kgu.traffic.global.domain.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    public ApiResponse<ReportDetailResponse> getReportDetail(@PathVariable Long id) {
        return new ApiResponse<>(reportService.getReportDetail(id));
    }


    @PatchMapping("/{id}")
    @Operation(summary = "신고 승인/반려 처리", description = "신고를 승인 또는 반려 처리합니다.")
    public ApiResponse<Void> processReport(
            @PathVariable Long id,
            @RequestBody @Valid ReportApproveRequest request // 필드 유효성 검사 활성화
    ) {
        reportService.processReport(id, request);
        return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }

    @GetMapping("/statistics")
    @Operation(summary = "신고 통계 조회", description = "전체 신고 수, 월간 신고 수, 승인/반려 수를 반환합니다.")
    public ApiResponse<ReportStatisticsResponse> getReportStatistics() {
        return new ApiResponse<>(reportService.getReportStatistics());
    }

    @PostMapping
    @Operation(summary = "신고 생성", description = "신규 신고를 생성하고 Firestore에 저장합니다.")
    public ApiResponse<Void> createReport(
            @RequestBody @Valid ReportCreateRequest request
    ) {
        reportService.createReport(request);
        return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }

    @GetMapping("/monthly")
    @Operation(summary = "이번 달 신고 목록 조회", description = "이번 달에 생성된 페이징된 신고 목록을 반환합니다.")
    public ApiResponse<ReportSimpleResponse> getMonthlyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedAt").descending());
        Page<ReportSimpleResponse> reports = reportService.getMonthlyReportsByRegion(pageable);
        return new ApiResponse<>(reports);
    }
}