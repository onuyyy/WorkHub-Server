package com.workhub.cs.dto;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CsPostResponse {

    private Long csPostId;
    private LocalDateTime deletedAt;
    private String title;
    private String content;
    private Long userId;
    private List<CsPostFileResponse> files;

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
