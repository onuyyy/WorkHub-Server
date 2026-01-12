package com.workhub.post.event;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostComment;

public record CommentCreatedEvent(Long projectId, Post post, PostComment comment) {
}
