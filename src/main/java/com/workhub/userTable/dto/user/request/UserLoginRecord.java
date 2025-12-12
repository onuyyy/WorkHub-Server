package com.workhub.userTable.dto.user.request;

public record UserLoginRecord(
        String loginId,
        String password
) {
}
