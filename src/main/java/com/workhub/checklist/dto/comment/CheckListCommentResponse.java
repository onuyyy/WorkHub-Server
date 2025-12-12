package com.workhub.checklist.dto.comment;

import com.workhub.checklist.entity.comment.CheckListItemComment;

import java.time.LocalDateTime;
import java.util.List;

public record CheckListCommentResponse(
        Long clCommentId,
        Long checkListItemId,
        Long userId,
        Long parentClCommentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CheckListCommentFileResponse> files
) {

    public static CheckListCommentResponse from(CheckListItemComment comment) {
        return new CheckListCommentResponse(
                comment.getClCommentId(),
                comment.getCheckListItemId(),
                comment.getUserId(),
                comment.getParentClCommentId(),
                comment.getClContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                List.of()
        );
    }

    public static CheckListCommentResponse from(CheckListItemComment comment, List<CheckListCommentFileResponse> files) {
        return new CheckListCommentResponse(
                comment.getClCommentId(),
                comment.getCheckListItemId(),
                comment.getUserId(),
                comment.getParentClCommentId(),
                comment.getClContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                files
        );
    }
}
