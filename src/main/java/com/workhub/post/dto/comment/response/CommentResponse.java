package com.workhub.post.dto.comment.response;

import com.workhub.post.entity.PostComment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long userId,
        Long parentCommentId,
        String commentContent,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        List<CommentResponse> children
) {
    public static CommentResponse from(PostComment comment){
        return new CommentResponse(
                comment.getCommentId(),
                comment.getPostId(),
                comment.getUserId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                new ArrayList<>()
        );
    }

    public CommentResponse withChildren(List<CommentResponse> children){
        return new CommentResponse(
                commentId,
                postId,
                userId,
                parentCommentId,
                commentContent,
                createAt,
                updateAt,
                children
        );
    }
}
