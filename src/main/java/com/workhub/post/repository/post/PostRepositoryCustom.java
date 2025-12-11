package com.workhub.post.repository.post;

import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> searchParentPosts(Long nodeId, String keyword, PostType postType, Pageable pageable);
}
