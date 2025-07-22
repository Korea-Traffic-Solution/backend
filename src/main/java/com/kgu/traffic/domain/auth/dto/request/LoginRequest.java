package com.kgu.traffic.domain.auth.dto.request;

public record LoginRequest(
        String loginId,
        String password
) {}