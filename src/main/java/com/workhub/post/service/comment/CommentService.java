package com.workhub.post.service.comment;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.PostComment;
import com.workhub.post.repository.comment.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;

    public PostComment save(PostComment postComment){return commentRepository.save(postComment);}

    public PostComment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_COMMENT));

    }

    public PostComment findByCommentAndMatchedUserId(Long commentId, Long userId) {
        PostComment comment = findById(commentId);
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_COMMENT_POST);
        }
        return comment;
    }

    public List<PostComment> findAllByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    public Page<PostComment> findPostWithReplies(Long postId, Pageable pageable) {
        return commentRepository.findByPostWithReplies(postId, pageable);
    }

    public List<PostComment> findByParentCommentId(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }
}
