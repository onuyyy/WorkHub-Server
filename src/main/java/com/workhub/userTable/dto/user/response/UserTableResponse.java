package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

@Builder
public record UserTableResponse(
        Long userId,
        String userName,
        String loginId,
        String email,
        String phone,
        UserRole role,
        Status status,
        String profileImg,
        Long companyId
) {
    public static UserTableResponse from(UserTable userTable) {
        return UserTableResponse.builder()
                .userId(userTable.getUserId())
                .userName(userTable.getUserName())
                .loginId(userTable.getLoginId())
                .email(userTable.getEmail())
                .phone(userTable.getPhone())
                .role(userTable.getRole())
                .status(userTable.getStatus())
                .profileImg(userTable.getProfileImg())
                .companyId(userTable.getCompanyId())
                .build();
    }
}
