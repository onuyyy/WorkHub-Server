package com.workhub.checklist.event;

import com.workhub.checklist.dto.checkList.CheckListItemStatus;

public record CheckListItemStatusChangedEvent(
        Long projectId,
        Long nodeId,
        Long checkListId,
        Long checkListItemId,
        String itemContent,
        CheckListItemStatus status
) {
}
