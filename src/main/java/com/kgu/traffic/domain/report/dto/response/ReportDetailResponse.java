package com.kgu.traffic.domain.report.dto.response;

import com.kgu.traffic.domain.report.entity.ReportStatus;
import java.time.LocalDateTime;

public record ReportDetailResponse(
        Long id,
        String title,
        String description,
        String reporterName,
        String targetName,
        ReportStatus status,
        LocalDateTime reportedAt
) {}