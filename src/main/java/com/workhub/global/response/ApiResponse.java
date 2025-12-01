package com.workhub.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private Boolean success;
    private String code;
    private String message;
    private T data;
    
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "SUCCESS", message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "CREATED", message, data));
    }

    public static ApiResponse<Object> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

}
