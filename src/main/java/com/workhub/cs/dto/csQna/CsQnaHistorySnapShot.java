package com.workhub.cs.dto.csQna;

import com.workhub.cs.entity.CsQna;
import lombok.Builder;

@Builder
public record CsQnaHistorySnapShot(
        Long csQnaId,
        String qnaContent,
        Long csPostId,
        Long userId,
        Long parentQnaId
) {
    public static CsQnaHistorySnapShot from(CsQna csQna){
        return CsQnaHistorySnapShot.builder()
                .csQnaId(csQna.getCsQnaId())
                .qnaContent(csQna.getQnaContent())
                .csPostId(csQna.getCsPostId())
                .userId(csQna.getUserId())
                .parentQnaId(csQna.getParentQnaId())
                .build();
    }
}
