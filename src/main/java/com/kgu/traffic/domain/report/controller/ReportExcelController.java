package com.kgu.traffic.domain.report.controller;

import com.kgu.traffic.domain.report.service.ReportExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class ReportExcelController {

    private final ReportExcelService reportExcelService;

    @GetMapping("/excel/download")
    public void downloadExcel(
            @RequestParam String brand,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response
    ) {
        reportExcelService.writeApprovedReportExcel(brand, date, response);
    }
}