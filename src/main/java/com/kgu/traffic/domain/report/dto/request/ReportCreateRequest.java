package com.kgu.traffic.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String reporterName,
        @NotBlank String targetName,
        @NotBlank String address,
        @NotBlank String gps,
        @NotBlank String brand,
        String imageUrl
) {}