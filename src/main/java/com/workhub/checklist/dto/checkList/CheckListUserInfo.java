package com.workhub.checklist.dto.checkList;

import com.workhub.userTable.dto.user.response.UserDetailResponse;

public record CheckListUserInfo(String userName, String userPhone) {

    public static CheckListUserInfo of(String userName, String userPhone) {
        return new CheckListUserInfo(userName, userPhone);
    }

    public static CheckListUserInfo of(UserDetailResponse user) {
        return new CheckListUserInfo(user.userName(), user.phone());
    }

    public static CheckListUserInfo empty() {
        return new CheckListUserInfo(null, null);
    }
}
