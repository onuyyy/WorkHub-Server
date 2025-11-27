package com.workhub.cs.dto;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
public record CsPostResponse(
        Long csPostId,
        LocalDateTime deletedAt,
        String title,
        String content,
        Long userId,
        List<CsPostFileResponse> files
) {

    public static CsPostResponse from(CsPost post, List<CsPostFile> fileList) {

        List<CsPostFileResponse> fileResponses =
                (fileList == null) ? Collections.emptyList() :
                        fileList.stream()
                                .map(CsPostFileResponse::from)
                                .toList();

        return CsPostResponse.builder()
                .csPostId(post.getCsPostId())
                .deletedAt(post.getDeletedAt())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUserId())
                .files(fileResponses)
                .build();
    }
}