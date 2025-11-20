package com.workhub.global.error.exception;

import com.workhub.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Runtime Exception 예외처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> runtimeExceptionHandler(RuntimeException e) {
        log.error("Runtime Exception - Message: {}", e.getMessage(), e);
        ApiResponse response = ApiResponse.error("E-000", "서버 에러가 발생했습니다. 담당자에게 문의 바랍니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}