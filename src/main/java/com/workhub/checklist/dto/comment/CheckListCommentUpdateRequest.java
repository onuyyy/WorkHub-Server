package com.workhub.checklist.dto.comment;

import lombok.Builder;

@Builder
public record CheckListCommentUpdateRequest(
        String content
) {
}
