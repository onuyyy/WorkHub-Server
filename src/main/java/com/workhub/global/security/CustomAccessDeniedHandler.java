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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증은 되었지만 권한이 없는 사용자가 리소스에 접근할 때 처리하는 핸들러
 * 403 Forbidden 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("접근 권한 없음: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());

        // ADMIN 권한이 필요한 경우 더 구체적인 에러 메시지 제공
        ErrorCode errorCode = ErrorCode.FORBIDDEN_ADMIN;

        ApiResponse<Void> apiResponse = ApiResponse.error(
            errorCode.getErrorCode(),
            errorCode.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
