package com.kgu.traffic.domain.notice.controller;

import com.kgu.traffic.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final RestTemplate restTemplate;

    @Value("${notice.url}")
    private String noticeUrl;

    @GetMapping
    public ApiResponse<String> getNotices() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(noticeUrl, String.class);
            return new ApiResponse<>(response.getBody());
        } catch (Exception e) {
            return new ApiResponse<>("공지사항 불러오기 실패: " + e.getMessage());
        }
    }
}