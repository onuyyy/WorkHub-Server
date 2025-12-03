package com.workhub.cs.dto.csQna;

import com.workhub.cs.entity.CsQna;

import java.time.LocalDateTime;

public record CsQnaResponse(
        Long csQnaId,
        Long csPostId,
        Long userId,
        Long parentQnaId,
        String qnaContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CsQnaResponse from(CsQna csQna) {
        return new CsQnaResponse(
                csQna.getCsQnaId(),
                csQna.getCsPostId(),
                csQna.getUserId(),
                csQna.getParentQnaId(),
                csQna.getQnaContent(),
                csQna.getCreatedAt(),
                csQna.getUpdatedAt()
        );
    }
}
