package com.workhub.checklist.dto;

import com.workhub.checklist.entity.CheckList;

import java.util.List;

public record CheckListResponse(
        Long checkListId,
        String description,
        Long projectNodeId,
        Long userId,
        List<CheckListItemResponse> items
) {
    public static CheckListResponse from(CheckList checkList, List<CheckListItemResponse> items) {
        return new CheckListResponse(
                checkList.getCheckListId(),
                checkList.getCheckListDescription(),
                checkList.getProjectNodeId(),
                checkList.getUserId(),
                items
        );
    }
}
