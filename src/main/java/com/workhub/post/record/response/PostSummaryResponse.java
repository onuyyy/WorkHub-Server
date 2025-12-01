package com.workhub.post.record.response;

import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;

import java.time.LocalDateTime;

/** 게시글 목록에서 필요한 요약 정보를 담는 DTO */
public record PostSummaryResponse(
        Long postId,
        PostType postType,
        HashTag hashTag,
        String title,
        String contentPreview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final int PREVIEW_LENGTH = 120;

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getPostId(),
                post.getType(),
                post.getHashtag(),
                post.getTitle(),
                buildPreview(post.getContent()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    /** 본문의 앞부분만 잘라낸 미리보기 문자열을 만든다. */
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
