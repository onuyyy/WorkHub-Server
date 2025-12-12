package com.workhub.checklist.dto.comment;

import com.workhub.checklist.entity.comment.CheckListItemCommentFile;

public record CheckListCommentFileResponse(
        Long commentFileId,
        String fileName,
        Integer fileOrder
) {
    public static CheckListCommentFileResponse from(CheckListItemCommentFile file) {
        return new CheckListCommentFileResponse(
                file.getCommentFileId(),
                file.getFileName(),
                file.getFileOrder()
        );
    }
}