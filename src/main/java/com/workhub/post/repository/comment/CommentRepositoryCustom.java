package com.workhub.post.repository.comment;

import com.workhub.post.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom {
    Page<PostComment> findByPostWithReplies(Long postId, Pageable pageable);

    List<PostComment> findAllByPostId(Long postId);
}
