package com.workhub.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CheckListOptionRequest(
        @NotBlank(message = "선택지 내용을 입력해주세요")
        @Size(max = 300, message = "내용은 300자 이하여야 합니다")
        String optionContent,

        @NotNull(message = "순서는 필수입니다")
        Integer optionOrder,

        List<String> fileUrls
) {
}