package com.workhub.checklist.dto.comment;

import com.workhub.checklist.entity.CheckListItemComment;

import java.time.LocalDateTime;

public record CheckListCommentResponse(
        Long clCommentId,
        Long checkListItemId,
        Long userId,
        Long parentClCommentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CheckListCommentResponse from(CheckListItemComment comment) {
        return new CheckListCommentResponse(
                comment.getClCommentId(),
                comment.getCheckListItemId(),
                comment.getUserId(),
                comment.getParentClCommentId(),
                comment.getClContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
