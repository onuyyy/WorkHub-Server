package com.workhub.cs.dto.csQna;

import jakarta.validation.constraints.NotBlank;

public record CsQnaRequest(
        @NotBlank(message = "CS 댓글 내용은 필수입니다.")
        String qnaContent,
        Long parentQnaId
) {
}
