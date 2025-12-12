package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record UserNameResponse(
        Long userId,
        String userName
) {
    public static UserNameResponse from(UserTable user) {
        return UserNameResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .build();
    }
}
