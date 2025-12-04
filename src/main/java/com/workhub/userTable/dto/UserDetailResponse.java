package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;

public record UserDetailResponse(
        Long userId,
        String loginId,
        String email,
        String phone,
        UserRole role,
        Status status,
        Long companyId
){
    public static UserDetailResponse from(UserTable userTable){
        return new UserDetailResponse(
                userTable.getUserId(),
                userTable.getLoginId(),
                userTable.getEmail(),
                userTable.getPhone(),
                userTable.getRole(),
                userTable.getStatus(),
                userTable.getCompanyId()
        );
    }
}
