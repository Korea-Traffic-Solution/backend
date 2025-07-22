package com.kgu.traffic.domain.auth.controller;

import com.kgu.traffic.domain.auth.dto.request.SignUpRequest;
import com.kgu.traffic.domain.auth.dto.request.LoginRequest;
import com.kgu.traffic.domain.auth.dto.response.LoginResponse;
import com.kgu.traffic.domain.auth.service.AuthService;
import com.kgu.traffic.global.domain.SuccessCode;
import com.kgu.traffic.global.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "관리자 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "관리자 회원가입 API", description = "관리자가 loginId, password, name, region을 입력하여 회원가입합니다.")
    public ApiResponse<Void> signUpAdmin(@Valid @RequestBody SignUpRequest request) {
        authService.signup(request);
        return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인 API", description = "loginId, password로 로그인하여 JWT 토큰을 응답받습니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return new ApiResponse<>(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "클라이언트의 JWT를 삭제하도록 유도합니다.")
    public ApiResponse<Void> logout() {
        return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }
}