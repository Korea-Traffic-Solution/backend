package com.kgu.traffic.domain.report.service;

import com.kgu.traffic.domain.auth.entity.Admin;
import com.kgu.traffic.domain.report.entity.Report;
import com.kgu.traffic.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExcelService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public void writeApprovedReportExcel(String brand, LocalDate date, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("승인된 신고 목록");

            // 헤더
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "지번주소", "GPS", "사유", "벌금", "신고일", "관리자 이름", "소속", "브랜드", "승인일"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            List<Report> reports = (date != null)
                    ? reportRepository.findApprovedByBrandAndDate(brand, date)
                    : reportRepository.findApprovedByBrand(brand);

            for (int i = 0; i < reports.size(); i++) {
                Report report = reports.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getAddress());
                row.createCell(2).setCellValue(report.getGps());
                row.createCell(3).setCellValue(report.getReason());
                row.createCell(4).setCellValue(report.getFine());
                row.createCell(5).setCellValue(report.getReportedAt().toString());
                Admin admin = report.getAdmin();
                row.createCell(6).setCellValue(admin != null ? admin.getName() : "");
                row.createCell(7).setCellValue(admin != null ? admin.getDepartment() : "");
                row.createCell(8).setCellValue(report.getBrand());
                row.createCell(9).setCellValue(report.getApprovedAt() != null ? report.getApprovedAt().toString() : "");
            }

            // 응답 헤더 설정
            String fileName = URLEncoder.encode("승인된_신고_목록.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            OutputStream out = response.getOutputStream();
            workbook.write(out);
        } catch (Exception e) {
            throw new RuntimeException("엑셀 다운로드 실패", e);
        }
    }
}