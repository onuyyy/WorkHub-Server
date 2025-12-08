package com.workhub.post.repository.post;

import com.workhub.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Optional<Post> findByPostIdAndDeletedAtIsNull(Long postId);
    boolean existsByPostIdAndDeletedAtIsNull(Long postId);

    List<Post> findByParentPostIdInAndDeletedAtIsNull(Collection<Long> parentIds);

    List<Post> findByParentPostIdAndDeletedAtIsNull(Long parentPostId);
}
