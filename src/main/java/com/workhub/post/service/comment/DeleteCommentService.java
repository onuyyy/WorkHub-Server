package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCommentService {
    private final CommentService commentService;
    private final HistoryRecorder historyRecorder;

    /**
     * 댓글과 자식 댓글을 삭제(소프트 딜리트)하고 히스토리를 기록한다.
     *
     * @param projectId 프로젝트 식별자
     * @param postId 게시글 식별자
     * @param commentId 댓글 식별자
     * @param userId 사용자 식별자
     * @return 삭제된 댓글의 게시글 ID
     */
    public Long delete(Long projectId, Long postId, Long commentId, Long userId) {
        PostComment postComment = commentService.findByCommentAndMatchedUserId(commentId, userId);

        if (!postId.equals(postComment.getPostId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_COMMENT_POST);
        }

        if (postComment.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_POST);
        }

        deleteWithChildren(postComment);

        return postComment.getPostId();
    }

    /**
     * 자식 댓글까지 재귀적으로 삭제 처리한다.
     */
    private void deleteWithChildren(PostComment comment) {
        List<PostComment> children = commentService.findByParentCommentId(comment.getCommentId());

        for (PostComment child : children) {
            deleteWithChildren(child);
        }
        snapshotAndRecordHistory(comment, ActionType.DELETE);

        comment.markDeleted();
    }

    private void snapshotAndRecordHistory(PostComment comment, ActionType actionType) {
        CommentHistorySnapshot snapshot = CommentHistorySnapshot.from(comment);
        historyRecorder.recordHistory(HistoryType.POST_COMMENT, comment.getCommentId(), actionType, snapshot);
    }
}
