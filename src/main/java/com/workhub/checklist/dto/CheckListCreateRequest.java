package com.workhub.checklist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CheckListCreateRequest(
        @NotBlank(message = "전달사항을 입력해주세요")
        @Size(max = 500, message = "전달사항은 500자 이하여야 합니다")
        String description,

        @Valid
        @Size(min = 1, message = "최소 1개의 항목이 필요합니다")
        List<CheckListItemRequest> items
) {
}
