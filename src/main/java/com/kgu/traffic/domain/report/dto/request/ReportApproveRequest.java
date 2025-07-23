package com.kgu.traffic.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "신고 승인/반려 요청")
public record ReportApproveRequest(

        @Schema(description = "승인 여부", example = "true")
        boolean approve,

        @Schema(description = "벌금 (0원이면 0으로 입력)", example = "50000")
        @NotNull(message = "벌금은 필수입니다.")
        @Min(value = 0, message = "벌금은 0원 이상이어야 합니다.")
        Integer fine,

        @Schema(description = "사유", example = "광고 스팸 신고")
        @NotBlank(message = "사유는 필수입니다.")
        String reason
) {}