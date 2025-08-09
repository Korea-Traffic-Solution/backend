package com.kgu.traffic.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Firestore Conclusion 원본 응답(그대로)")
public record ReportDetailResponse(
        @Schema(description = "문서 ID") String id,

        // ===== Conclusion 컬렉션 필드들 =====
        @Schema(description = "AI 결론") List<String> aiConclusion,
        @Schema(description = "신뢰도") Double confidence,
        @Schema(description = "원본 날짜(문자열 그대로)") String date,
        @Schema(description = "감지 브랜드") String detectedBrand,
        @Schema(description = "GPS 정보") String gpsInfo,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "지역") String region,
        @Schema(description = "리포트 이미지 URL") String reportImgUrl,
        @Schema(description = "결과") String result,
        @Schema(description = "사용자/신고자 ID") String userId,
        @Schema(description = "위반 내용") String violation
) {}