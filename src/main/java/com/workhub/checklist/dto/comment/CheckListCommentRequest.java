package com.workhub.checklist.dto.comment;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record CheckListCommentRequest(
    String content,
    Long patentClCommentId,

    @Valid
    List<CheckListCommentFileRequest> files
) {
}
