package com.workhub.checklist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CheckListItemRequest(
        @NotBlank(message = "항목 제목을 입력해주세요")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String itemTitle,

        @NotNull(message = "순서는 필수입니다")
        Integer itemOrder,

        Long templateId,

        @Valid
        @Size(min = 1, message = "최소 1개의 선택지가 필요합니다")
        List<CheckListOptionRequest> options  // 선택지들
) {
}
