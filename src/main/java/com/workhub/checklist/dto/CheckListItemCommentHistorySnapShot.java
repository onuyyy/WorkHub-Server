package com.workhub.checklist.dto;

import com.workhub.checklist.entity.CheckListItemComment;
import lombok.Builder;

@Builder
public record CheckListItemCommentHistorySnapShot(
        Long clCommentId,
        String clContent,
        Long checkListItemId,
        Long userId,
        Long parentClCommentId
) {
    public static CheckListItemCommentHistorySnapShot from(CheckListItemComment comment) {
        return CheckListItemCommentHistorySnapShot.builder()
                .clCommentId(comment.getClCommentId())
                .clContent(comment.getClContent())
                .checkListItemId(comment.getCheckListItemId())
                .userId(comment.getUserId())
                .parentClCommentId(comment.getParentClCommentId())
                .build();
    }
}
