package com.workhub.userTable.dto;

import com.workhub.userTable.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull(message = "변경할 권한을 선택해주세요.")
        UserRole role
) {
}
