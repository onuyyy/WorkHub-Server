package com.workhub.post.repository;

import com.workhub.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);
    boolean existsByPostIdAndDeletedAtIsNull(Long postId);
}
