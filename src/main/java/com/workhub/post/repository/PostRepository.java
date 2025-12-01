package com.workhub.post.repository;

import com.workhub.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);
    boolean existsByPostIdAndDeletedAtIsNull(Long postId);
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM post WHERE post_id = :postId", nativeQuery = true)
    boolean existsByPostIdIncludingDeleted(@Param("postId") Long postId);
}
