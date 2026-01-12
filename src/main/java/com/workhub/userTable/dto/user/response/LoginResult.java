package com.workhub.userTable.dto.user.response;

import org.springframework.security.core.Authentication;

public record LoginResult(
        Authentication authentication,
        UserLoginResponse user
) {
}
