package com.workhub.post.dto.comment;

import com.workhub.post.entity.PostComment;

/**
 * 댓글 변경 이력을 위한 스냅샷.
 */
public record CommentHistorySnapshot(String content, Long postId, Long parentCommentId, Long userId) {
    public static CommentHistorySnapshot from(PostComment comment) {
        return new CommentHistorySnapshot(
                comment.getContent(),
                comment.getPostId(),
                comment.getParentCommentId(),
                comment.getUserId()
        );
    }
}
