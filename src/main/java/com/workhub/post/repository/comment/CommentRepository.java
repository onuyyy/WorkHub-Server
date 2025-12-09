package com.workhub.post.repository.comment;

import com.workhub.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<PostComment, Long>, CommentRepositoryCustom {
    List<PostComment> findByParentCommentId(Long parentCommentId);
}
