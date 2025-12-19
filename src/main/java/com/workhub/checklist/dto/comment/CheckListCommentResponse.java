package com.workhub.checklist.dto.comment;

import com.workhub.checklist.entity.comment.CheckListItemComment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CheckListCommentResponse(
        Long clCommentId,
        Long checkListItemId,
        Long userId,
        String userName,
        Long parentClCommentId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CheckListCommentFileResponse> files,
        List<CheckListCommentResponse> children
) {

    public static CheckListCommentResponse from(CheckListItemComment comment, String userName) {
        return new CheckListCommentResponse(
                comment.getClCommentId(),
                comment.getCheckListItemId(),
                comment.getUserId(),
                userName,
                comment.getParentClCommentId(),
                comment.getClContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                List.of(),
                new ArrayList<>()
        );
    }

    public static CheckListCommentResponse from(CheckListItemComment comment, String userName, List<CheckListCommentFileResponse> files) {
        return new CheckListCommentResponse(
                comment.getClCommentId(),
                comment.getCheckListItemId(),
                comment.getUserId(),
                userName,
                comment.getParentClCommentId(),
                comment.getClContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                files,
                new ArrayList<>()
        );
    }

    public CheckListCommentResponse withChildren(List<CheckListCommentResponse> children) {
        return new CheckListCommentResponse(
                clCommentId,
                checkListItemId,
                userId,
                userName,
                parentClCommentId,
                content,
                createdAt,
                updatedAt,
                files,
                children
        );
    }
}
