package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.dto.comment.request.CommentUpdateRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostComment;
import com.workhub.post.service.PostValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateCommentServiceTest {

    @Mock
    CommentService commentService;
    @Mock
    HistoryRecorder historyRecorder;
    @Mock
    PostValidator postValidator;

    @InjectMocks
    UpdateCommentService updateCommentService;

    @Test
    @DisplayName("다른 게시글 댓글이면 예외를 던진다")
    void update_withMismatchedPost_shouldThrow() {
        PostComment existing = mockComment(1L, 2L, "old");
        given(commentService.findByCommentAndMatchedUserId(1L, 3L)).willReturn(existing);
        given(postValidator.validatePostToProject(anyLong(), anyLong())).willReturn(Post.builder().build());

        assertThatThrownBy(() -> updateCommentService.update(10L, 1L, 99L, 3L, new CommentUpdateRequest("new")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_COMMENT_POST);
    }

    @Test
    @DisplayName("정상 수정 시 내용이 변경되고 히스토리가 기록된다")
    void update_success() {
        PostComment existing = mockComment(1L, 2L, "old");
        given(commentService.findByCommentAndMatchedUserId(1L, 3L)).willReturn(existing);
        CommentHistorySnapshot expectedSnapshot = CommentHistorySnapshot.from(existing);
        given(postValidator.validatePostToProject(anyLong(), anyLong())).willReturn(Post.builder().build());

        CommentResponse response = updateCommentService.update(10L, 1L, 2L, 3L, new CommentUpdateRequest("new"));

        assertThat(response.commentContent()).isEqualTo("new");
        verify(historyRecorder).recordHistory(
                eq(HistoryType.POST_COMMENT),
                eq(1L),
                eq(ActionType.UPDATE),
                eq(expectedSnapshot)
        );
    }

    private PostComment mockComment(Long commentId, Long postId, String content) {
        return PostComment.builder()
                .commentId(commentId)
                .postId(postId)
                .userId(3L)
                .content(content)
                .build();
    }
}
