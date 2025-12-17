package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckList;

import java.util.List;

public record CheckListResponse(
        Long checkListId,
        String description,
        Long projectNodeId,
        Long userId,
        String userName,
        String userPhone,
        List<CheckListItemResponse> items
) {
    public static CheckListResponse from(
            CheckList checkList,
            CheckListUserInfo userInfo,
            List<CheckListItemResponse> items
    ) {
        return new CheckListResponse(
                checkList.getCheckListId(),
                checkList.getCheckListDescription(),
                checkList.getProjectNodeId(),
                checkList.getUserId(),
                userInfo.userName(),
                userInfo.userPhone(),
                items
        );
    }
}
