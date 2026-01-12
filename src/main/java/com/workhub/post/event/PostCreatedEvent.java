package com.workhub.post.event;

import com.workhub.post.entity.Post;

public record PostCreatedEvent(Long projectId, Post post) {
}
