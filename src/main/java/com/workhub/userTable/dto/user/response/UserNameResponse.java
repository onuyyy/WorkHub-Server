package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record UserNameResponse(
        Long userId,
        String loginId,
        String userName,
        String profileImg
) {
    public static UserNameResponse from(UserTable user) {
        return UserNameResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userName(user.getUserName())
                .profileImg(user.getProfileImg())
                .build();
    }
}
