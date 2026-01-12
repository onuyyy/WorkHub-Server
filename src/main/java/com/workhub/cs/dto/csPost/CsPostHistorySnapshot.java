package com.workhub.cs.dto.csPost;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostStatus;
import lombok.Builder;

@Builder
public record CsPostHistorySnapshot(
        Long csPostId,
        String title,
        String content,
        CsPostStatus csPostStatus,
        Long projectId,
        Long userId
) {
    public static CsPostHistorySnapshot from(CsPost csPost){
        return CsPostHistorySnapshot.builder()
                .csPostId(csPost.getCsPostId())
                .title(csPost.getTitle())
                .content(csPost.getContent())
                .csPostStatus(csPost.getCsPostStatus())
                .projectId(csPost.getProjectId())
                .userId(csPost.getUserId())
                .build();
    }
}
