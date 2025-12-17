package com.workhub.checklist.dto.checkList;

public record CheckListUserInfo(String userName, String userPhone) {

    public static CheckListUserInfo of(String userName, String userPhone) {
        return new CheckListUserInfo(userName, userPhone);
    }

    public static CheckListUserInfo empty() {
        return new CheckListUserInfo(null, null);
    }
}
