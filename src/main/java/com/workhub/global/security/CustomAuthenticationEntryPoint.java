package com.workhub.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 처리하는 핸들러
 * 세션 만료 또는 로그인하지 않은 경우 401 Unauthorized 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        log.warn("인증되지 않은 요청: {} - {}", request.getRequestURI(), authException.getMessage());

        ErrorCode errorCode = ErrorCode.NOT_LOGGED_IN;

        // 세션이 만료된 경우 더 구체적인 에러 메시지 제공
        if (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()) {
            errorCode = ErrorCode.SESSION_EXPIRED;
            log.warn("세션 만료: sessionId={}", request.getRequestedSessionId());
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(
            errorCode.getErrorCode(),
            errorCode.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
