package com.workhub.userTable.controller;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.EmailVerificationApi;
import com.workhub.userTable.dto.EmailVerificationConfirmRequest;
import com.workhub.userTable.dto.EmailVerificationSendRequest;
import com.workhub.userTable.dto.EmailVerificationStatusResponse;
import com.workhub.userTable.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController implements EmailVerificationApi {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(@RequestBody @Valid EmailVerificationSendRequest request) {
        emailVerificationService.sendVerificationCode(request.email(), request.userName());
        return ApiResponse.success(null, "인증 코드가 발송되었습니다.");
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> confirm(@RequestBody @Valid EmailVerificationConfirmRequest request) {
        boolean verified = emailVerificationService.verifyCode(request.email(), request.code());
        if (!verified) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_CODE);
        }
        return ApiResponse.success(EmailVerificationStatusResponse.of(true), "이메일 인증이 완료되었습니다.");
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> status(@RequestParam("email") String email) {
        boolean verified = emailVerificationService.isVerified(email);
        return ApiResponse.success(EmailVerificationStatusResponse.of(verified), "이메일 인증 여부가 조회되었습니다.");
    }
}
