package com.kgu.traffic.domain.auth.dto.request;

public record SignUpRequest(
        String loginId,
        String password,
        String name,
        String department 
) {}