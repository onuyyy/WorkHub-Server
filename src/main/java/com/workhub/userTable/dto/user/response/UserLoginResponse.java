package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record UserLoginResponse(
        Long userId,
        String userName,
        String profileImg,
        String role
) {
    public static UserLoginResponse from(UserTable userTable) {
        return UserLoginResponse.builder()
                .userId(userTable.getUserId())
                .userName(userTable.getUserName())
                .profileImg(userTable.getProfileImg())
                .role(userTable.getRole().name())
                .build();
    }
}
