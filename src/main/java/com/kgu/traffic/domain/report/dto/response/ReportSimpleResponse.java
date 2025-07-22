package com.kgu.traffic.domain.report.dto.response;

import com.kgu.traffic.domain.report.entity.ReportStatus;
import java.time.LocalDateTime;

public record ReportSimpleResponse(
        Long id,
        String title,
        String reporterName,
        ReportStatus status,
        LocalDateTime reportedAt
) {}