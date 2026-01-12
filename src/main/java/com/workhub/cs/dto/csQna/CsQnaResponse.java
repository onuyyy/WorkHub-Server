package com.workhub.cs.dto.csQna;

import com.workhub.cs.entity.CsQna;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CsQnaResponse(
        Long csQnaId,
        Long csPostId,
        Long userId,
        String userName,
        Long parentQnaId,
        String qnaContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CsQnaResponse> children
) {
    public static CsQnaResponse from(CsQna csQna, String userName) {
        return new CsQnaResponse(
                csQna.getCsQnaId(),
                csQna.getCsPostId(),
                csQna.getUserId(),
                userName,
                csQna.getParentQnaId(),
                csQna.getQnaContent(),
                csQna.getCreatedAt(),
                csQna.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    public CsQnaResponse withChildren(List<CsQnaResponse> children) {
        return new CsQnaResponse(
                csQnaId,
                csPostId,
                userId,
                userName,
                parentQnaId,
                qnaContent,
                createdAt,
                updatedAt,
                children
        );
    }
}
