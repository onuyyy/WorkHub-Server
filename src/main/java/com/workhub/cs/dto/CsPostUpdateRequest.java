package com.workhub.cs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CsPostUpdateRequest(
        @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력값입니다.")
        String content,

        List<CsPostFileUpdateRequest> files
) {
}
