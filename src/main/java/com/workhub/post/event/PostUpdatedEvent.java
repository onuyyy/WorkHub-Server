package com.workhub.post.event;

import com.workhub.post.entity.Post;

public record PostUpdatedEvent(Long projectId, Post post) {
}
