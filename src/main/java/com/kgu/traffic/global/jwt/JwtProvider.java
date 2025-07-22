package com.kgu.traffic.global.jwt;

import com.kgu.traffic.domain.auth.entity.Admin;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private byte[] keyBytes;

    @PostConstruct
    protected void init() {
        keyBytes = secretKey.getBytes();
    }

    public String createToken(String loginId) {
        Date now = new Date();
        long expirationTime = 1000L * 60 * 60; // 1시간
        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getLoginId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyBytes)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(keyBytes).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}