package com.workhub.checklist.dto.comment;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record CheckListCommentUpdateRequest(
        String content,

        @Valid
        List<CheckListCommentFileRequest> files
) {
}
