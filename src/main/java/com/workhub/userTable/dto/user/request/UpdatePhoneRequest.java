package com.workhub.userTable.dto.user.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePhoneRequest(

        @NotBlank(message = "변경하려는 값은 빈 값일 수 없습니다.")
        String phone
) {
}
