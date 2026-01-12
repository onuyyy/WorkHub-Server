package com.workhub.checklist.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CheckListCommentFileRequest(
        @NotBlank String fileName,
        Integer fileOrder
) {
}