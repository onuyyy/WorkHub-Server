package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.dto.comment.request.CommentUpdateRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.PostComment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCommentService {
    private final CommentService commentService;
    private final HistoryRecorder historyRecorder;

    /**
     * 댓글을 수정하고 변경 전 내용을 히스토리에 저장한다.
     *
     * @param postCommentId 댓글 식별자
     * @param postId 게시글 식별자
     * @param userId 작성자 식별자
     * @param commentUpdateRequest 수정 요청 본문
     * @return 수정된 댓글 응답
     */
    public CommentResponse update(Long postCommentId, Long postId, Long userId, CommentUpdateRequest commentUpdateRequest) {
        PostComment postComment = commentService.findByCommentAndMatchedUserId(postCommentId, userId);

        validateCommentBelongs(postComment, postId);

        snapshotAndRecordHistory(postComment, ActionType.UPDATE);

        postComment.updateContent(commentUpdateRequest.commentContext());
        return CommentResponse.from(postComment);
    }

    /**
     * 댓글이 요청한 게시글에 속하는지 검증한다.
     */
    public void validateCommentBelongs(PostComment postComment, Long postId) {
        if (!postComment.getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_COMMENT_POST);
        }

    }

    private void snapshotAndRecordHistory(PostComment comment, ActionType actionType) {
        CommentHistorySnapshot snapshot = CommentHistorySnapshot.from(comment);
        historyRecorder.recordHistory(HistoryType.POST_COMMENT, comment.getCommentId(), actionType, snapshot);
    }
}
