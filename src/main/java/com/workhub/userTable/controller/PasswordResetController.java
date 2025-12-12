package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.PasswordResetApi;
import com.workhub.userTable.dto.user.request.PasswordResetConfirmRequest;
import com.workhub.userTable.dto.user.request.PasswordResetSendRequest;
import com.workhub.userTable.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password-reset")
@RequiredArgsConstructor
public class PasswordResetController implements PasswordResetApi {

    private final PasswordResetService passwordResetService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendResetCode(@Valid @RequestBody PasswordResetSendRequest request) {
        passwordResetService.sendResetCode(request);
        return ApiResponse.success(null, "비밀번호 재설정 인증 코드가 발송되었습니다.");
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.success(null, "비밀번호가 재설정되었습니다.");
    }
}
