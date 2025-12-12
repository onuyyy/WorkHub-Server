package com.workhub.checklist.dto.comment;

import lombok.Builder;

@Builder
public record CheckListCommentRequest(
    String content,
    Long patentClCommentId
) {
}
