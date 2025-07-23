package com.kgu.traffic.domain.report.dto.response;

public record ReportStatisticsResponse(
        long totalCount,
        long monthlyCount,
        long approvedCount,
        long rejectedCount
) {}