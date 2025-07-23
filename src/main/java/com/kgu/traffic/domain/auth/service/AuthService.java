package com.kgu.traffic.domain.auth.service;

import com.kgu.traffic.domain.auth.dto.request.LoginRequest;
import com.kgu.traffic.domain.auth.dto.request.SignUpRequest;
import com.kgu.traffic.domain.auth.dto.response.LoginResponse;
import com.kgu.traffic.domain.auth.entity.Admin;
import com.kgu.traffic.domain.auth.repository.AdminRepository;
import com.kgu.traffic.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignUpRequest request) {
        if (adminRepository.findByLoginId(request.loginId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 ID입니다.");
        }

        Admin admin = Admin.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .department(request.department())
                .build();
        adminRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID입니다."));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.createToken(admin.getLoginId());
        return new LoginResponse(token, admin.getName(), admin.getDepartment());
    }
}