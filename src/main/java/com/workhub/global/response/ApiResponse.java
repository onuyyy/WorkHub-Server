package com.workhub.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private Boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> success(T data,String message) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, "CREATED", message, data);
    }

    public static ApiResponse error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

}
