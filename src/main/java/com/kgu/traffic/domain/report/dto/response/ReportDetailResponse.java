package com.kgu.traffic.domain.report.dto.response;

import com.kgu.traffic.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "신고 상세 응답")
public record ReportDetailResponse(
        @Schema(description = "신고 ID (Firestore 문서 ID)")
        String id,

        @Schema(description = "제목")
        String title,

        @Schema(description = "설명")
        String description,

        @Schema(description = "신고자 이름")
        String reporterName,

        @Schema(description = "대상 이름")
        String targetName,

        @Schema(description = "상태")
        ReportStatus status,

        @Schema(description = "신고일")
        LocalDateTime reportedAt,

        @Schema(description = "주소")
        String address,

        @Schema(description = "GPS")
        String gps,

        @Schema(description = "사유")
        String reason,

        @Schema(description = "벌금")
        Integer fine,

        @Schema(description = "브랜드")
        String brand,

        @Schema(description = "승인일")
        LocalDateTime approvedAt,

        @Schema(description = "AI 분석 결과")
        List<String> aiConclusion,

        @Schema(description = "감지 브랜드")
        String detectedBrand,

        @Schema(description = "위치")
        String location,

        @Schema(description = "신고 내용")
        String reportContent,

        @Schema(description = "신고 이미지 URL")
        String reportImgUrl
) {}