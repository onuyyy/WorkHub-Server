package com.workhub.history.dto;

import com.workhub.userTable.entity.UserTable;
import lombok.Builder;
import lombok.Getter;

/**
 * 히스토리 응답에 포함될 사용자 정보
 */
@Getter
@Builder
public class UserInfo {

    private Long userId;
    private String userName;

    public static UserInfo from(UserTable user) {
        if (user == null) {
            return null;
        }
        return UserInfo.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .build();
    }
}