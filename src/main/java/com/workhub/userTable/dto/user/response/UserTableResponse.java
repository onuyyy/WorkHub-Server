package com.workhub.userTable.dto.user.response;

import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;

public record UserTableResponse(
        Long userId,
        String userName,
        String loginId,
        String email,
        String phone,
        UserRole role,
        Status status,
        Long companyId
) {
    public static UserTableResponse from(UserTable userTable) {
        return new UserTableResponse(
                userTable.getUserId(),
                userTable.getUserName(),
                userTable.getLoginId(),
                userTable.getEmail(),
                userTable.getPhone(),
                userTable.getRole(),
                userTable.getStatus(),
                userTable.getCompanyId()
        );
    }
}
