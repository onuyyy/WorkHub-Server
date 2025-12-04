package com.workhub.post.repository;

import com.workhub.post.entity.PostLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLinkRepository extends JpaRepository<PostLink, Long> {
    List<PostLink> findByPostId(Long postId);
}
