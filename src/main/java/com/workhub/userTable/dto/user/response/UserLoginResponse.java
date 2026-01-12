package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record UserLoginResponse(
        Long userId,
        String loginId,
        String userName,
        String profileImg,
        String role,
        String email,
        String phone
) {
    public static UserLoginResponse from(UserTable userTable) {
        return UserLoginResponse.builder()
                .userId(userTable.getUserId())
                .loginId(userTable.getLoginId())
                .userName(userTable.getUserName())
                .profileImg(userTable.getProfileImg())
                .role(userTable.getRole().name())
                .email(userTable.getEmail())
                .phone(userTable.getPhone())
                .build();
    }
}
