package com.workhub.userTable.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetSendRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email
        @Size(max = 50, message = "이메일은 50자 이하로 입력해주세요.")
        String email
) {
}
