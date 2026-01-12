package com.workhub.userTable.dto.user.request;

import com.workhub.userTable.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterRecord(
        @NotBlank
        @Size(max = 30)
        String loginId,

        @NotBlank
        String password,

        @NotBlank
        @Size(max = 20)
        String userName,

        @NotBlank
        @Email
        @Size(max = 50)
        String email,

        @NotBlank
        @Size(max = 12)
        String phone,

        @NotNull
        Long companyId,

        @NotNull
        UserRole role
) {
}
