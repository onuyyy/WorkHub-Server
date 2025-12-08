package com.workhub.post.record.history;

import com.workhub.post.entity.PostType;

public record PostHistorySnapshot(
        String title,
        String content,
        PostType postType,
        String postIp,
        Long parentPostId
) {
    public static PostHistorySnapshot from(com.workhub.post.entity.Post post) {
        return new PostHistorySnapshot(
                post.getTitle(),
                post.getContent(),
                post.getType(),
                post.getPostIp(),
                post.getParentPostId()
        );
    }
}
