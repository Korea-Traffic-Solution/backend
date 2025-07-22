package com.kgu.traffic.domain.auth.dto.response;

public record LoginResponse(
        String token,
        String name,
        String region
) {}