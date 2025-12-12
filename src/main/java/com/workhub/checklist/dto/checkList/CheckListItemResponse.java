package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckListItem;

import java.time.LocalDateTime;
import java.util.List;

public record CheckListItemResponse(
        Long checkListItemId,
        String itemTitle,
        Integer itemOrder,
        CheckListItemStatus status,
        LocalDateTime confirmedAt,
        Long templateId,
        List<CheckListOptionResponse> options
) {
    public static CheckListItemResponse from(CheckListItem item, List<CheckListOptionResponse> options) {
        return new CheckListItemResponse(
                item.getCheckListItemId(),
                item.getItemTitle(),
                item.getItemOrder(),
                item.getStatus(),
                item.getConfirmedAt(),
                item.getTemplateId(),
                options
        );
    }
}