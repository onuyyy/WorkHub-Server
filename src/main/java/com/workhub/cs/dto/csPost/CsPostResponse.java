package com.workhub.cs.dto.csPost;

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
        String userName,
        List<CsPostFileResponse> files,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CsPostResponse from(CsPost post, List<CsPostFile> fileList, String userName) {

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
                .userName(userName)
                .files(fileResponses)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public static CsPostResponse from(CsPost post, String userName) {
        return CsPostResponse.builder()
                .csPostId(post.getCsPostId())
                .deletedAt(post.getDeletedAt())
                .title(post.getTitle())
                .content(post.getContent())
                .userId(post.getUserId())
                .userName(userName)
                .files(Collections.emptyList()) // 리스트 조회에서는 파일 불러오지 않음
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
