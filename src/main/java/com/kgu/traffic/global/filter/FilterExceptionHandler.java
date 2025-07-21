package com.kgu.traffic.global.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import com.kgu.traffic.global.dto.response.ApiResponse;
import com.kgu.traffic.global.exception.ErrorCode;
import com.kgu.traffic.global.exception.TrafficException;

/**
 * 전역 필터 예외 처리 클래스
 *
 * <p>Spring Security 또는 Servlet Filter에서 발생한 예외를 잡아, 사용자에게 일관된 JSON 에러 응답을 반환하기 위한 필터입니다. 커스텀
 * 예외(LifelineException)는 정의된 에러 코드에 따라 처리되며, 그 외 예외는 내부 서버 오류(500)로 응답합니다.
 */
@Slf4j
public class FilterExceptionHandler extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (TrafficException ex) {
            // LifelineException 예외를 처리하여 JSON 응답으로 전송
            createAPIResponse(response, ex.getErrorCode());
        } catch (Exception ex) {
            // 기타 예외 처리
            log.error("Unhandled exception occurred: ", ex);
            createAPIResponse(response, ErrorCode.INTERNAL_SEVER_ERROR);
        }
    }

    private void createAPIResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        ApiResponse<Void> apiResponse = new ApiResponse<>(errorCode);
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
