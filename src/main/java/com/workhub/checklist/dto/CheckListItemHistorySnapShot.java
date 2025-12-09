package com.workhub.checklist.dto;

import com.workhub.checklist.entity.CheckListItem;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CheckListItemHistorySnapShot(
        Long checkListItemId,
        String itemTitle,
        Integer itemOrder,
        Boolean confirm,
        LocalDateTime confirmedAt,
        Long checkListId,
        Long templateId,
        Long userId
) {
    public static CheckListItemHistorySnapShot from(CheckListItem item) {
        return CheckListItemHistorySnapShot.builder()
                .checkListItemId(item.getCheckListItemId())
                .itemTitle(item.getItemTitle())
                .itemOrder(item.getItemOrder())
                .confirm(item.getConfirm())
                .confirmedAt(item.getConfirmedAt())
                .checkListId(item.getCheckListId())
                .templateId(item.getTemplateId())
                .userId(item.getUserId())
                .build();
    }
}
