package com.workhub.userTable.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyCodeRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email
        @Size(max = 50, message = "이메일은 50자 이하로 입력해주세요.")
        String email,

        @NotBlank(message = "인증 코드를 입력해주세요.")
        @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자입니다.")
        String verificationCode
        ) {
}
