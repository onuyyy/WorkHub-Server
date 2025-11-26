package com.workhub.post.record.response;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;

public record PostResponse (
        Long id,
        PostType postType,
        String title,
        String content,
        String postIp,
        Long parentPostId
){
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getType(),
                post.getTitle(),
                post.getContent(),
                post.getPostIp(),
                post.getParentPostId() != null ? post.getParentPostId().getId() : null
        );
    }
}
