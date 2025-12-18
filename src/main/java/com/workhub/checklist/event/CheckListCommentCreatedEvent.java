package com.workhub.checklist.event;

public record CheckListCommentCreatedEvent(
        Long projectId,
        Long nodeId,
        Long checkListId,
        Long checkListItemId,
        Long commentId,
        String content,
        Long checklistOwnerId,
        Long parentCommentAuthorId,
        Long authorId
) {
}
