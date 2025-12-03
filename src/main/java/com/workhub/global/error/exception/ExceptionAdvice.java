package com.workhub.global.error.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "")
public class ExceptionAdvice {

    // Business Exception 예외처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(BusinessException e) {
        log.error("Business Exception - Code: {}, Message: {}", e.getErrorCode().getErrorCode(), e.getMessage());
        ApiResponse response = ApiResponse.error(e.getErrorCode().getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

    // Controller Exception 예외처리
    @ExceptionHandler(ControllerException.class)
    public ResponseEntity<ApiResponse> handleControllerException(ControllerException e) {
        log.error("Controller Exception - Code: {}, Message: {}", e.getErrorCode().getErrorCode(), e.getMessage());
        ApiResponse response = ApiResponse.error(e.getErrorCode().getErrorCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

    // Authorization Denied Exception 예외처리
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.error("Authorization Denied - Message: {}", e.getMessage());
        ApiResponse response = ApiResponse.error(ErrorCode.FORBIDDEN_ADMIN.getErrorCode(),  ErrorCode.FORBIDDEN_ADMIN.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // HTTP Message Not Readable Exception 예외처리 (JSON 파싱 실패)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("Message Not Readable - Message: {}", e.getMessage());

        // enum 파싱 실패인지 확인
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) e.getCause();
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                ApiResponse response = ApiResponse.error(
                    ErrorCode.INVALID_ENUM_VALUE.getErrorCode(),
                    ErrorCode.INVALID_ENUM_VALUE.getMessage()
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        // 기타 JSON 파싱 오류
        ApiResponse response = ApiResponse.error(
            ErrorCode.INVALID_REQUEST_FORMAT.getErrorCode(),
            ErrorCode.INVALID_REQUEST_FORMAT.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Runtime Exception 예외처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> runtimeExceptionHandler(RuntimeException e) {
        log.error("Runtime Exception - Message: {}", e.getMessage(), e);
        ApiResponse response = ApiResponse.error("E-000", "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}