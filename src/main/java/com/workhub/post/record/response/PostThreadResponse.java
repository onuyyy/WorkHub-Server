package com.workhub.post.record.response;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;

import java.time.LocalDateTime;
import java.util.List;

/** 계층형 게시글 응답 DTO */
public record PostThreadResponse(
        Long postId,
        Long parentPostId,
        PostType postType,
        String title,
        String contentPreview,
        LocalDateTime createdAt,
        List<PostThreadResponse> replies
) {
    private static final int PREVIEW_LENGTH = 120;

    public static PostThreadResponse from(Post post, List<PostThreadResponse> replies) {
        return new PostThreadResponse(
                post.getPostId(),
                post.getParentPostId(),
                post.getType(),
                post.getTitle(),
                buildPreview(post.getContent()),
                post.getCreatedAt(),
                replies
        );
    }

    private static String buildPreview(String content) {
        if (content == null) {
            return "";
        }
        if (content.length() <= PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_LENGTH) + "...";
    }
}
