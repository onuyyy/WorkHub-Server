package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.dto.comment.request.CommentRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.PostComment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCommentService {
    private final CommentService commentService;
    private final HistoryRecorder historyRecorder;

    /**
     * 댓글을 생성하고 히스토리를 기록한다.
     *
     * @param projectId 프로젝트 식별자
     * @param postId 게시글 식별자
     * @param userId 작성자 식별자
     * @param commentRequest 생성 요청 본문
     * @return 생성된 댓글 응답
     */
    public CommentResponse create(Long projectId, Long postId, Long userId, CommentRequest commentRequest) {
        validateContent(commentRequest.content());

        Long parentCommentId = resolveParent(postId, commentRequest.parentCommentId());
        PostComment postComment = PostComment.of(postId, userId, parentCommentId, commentRequest.content());
        postComment =  commentService.save(postComment);

        snapshotAndRecordHistory(postComment, ActionType.CREATE);
        return CommentResponse.from(postComment);
    }

    /**
     * 댓글 본문 유효성을 검증한다.
     */
    private void validateContent(String content){
        if ((content == null) || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_CONTENT);
        }
    }

    /**
     * 부모 댓글 존재 여부와 동일 게시글 여부를 검증한다.
     */
    private Long resolveParent(Long postId, Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        PostComment parent = commentService.findById(parentCommentId);
        if (!postId.equals(parent.getPostId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_COMMENT_POST);
        }
        return parent.getCommentId();
    }

    /**
     * 댓글을 스냅샷으로 변환해 히스토리에 저장한다.
     */
    private void snapshotAndRecordHistory(PostComment comment, ActionType actionType) {
        CommentHistorySnapshot snapshot = CommentHistorySnapshot.from(comment);
        historyRecorder.recordHistory(HistoryType.POST_COMMENT, comment.getCommentId(), actionType, snapshot);
    }
}
