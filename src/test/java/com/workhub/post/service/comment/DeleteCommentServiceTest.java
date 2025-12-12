package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostComment;
import com.workhub.post.service.PostValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteCommentServiceTest {

    @Mock
    CommentService commentService;
    @Mock
    HistoryRecorder historyRecorder;
    @Mock
    PostValidator postValidator;

    @InjectMocks
    DeleteCommentService deleteCommentService;

    @BeforeEach
    void setUp() {
        given(postValidator.validatePostToProject(anyLong(), anyLong()))
                .willReturn(Post.builder().build());
    }

    @Test
    @DisplayName("다른 게시글 댓글이면 삭제 시 예외를 던진다")
    void delete_withMismatchedPost_shouldThrow() {
        PostComment comment = mockComment(1L, 2L, "content");
        given(commentService.findByCommentAndMatchedUserId(1L, 3L)).willReturn(comment);

        assertThatThrownBy(() -> deleteCommentService.delete(10L, 99L, 1L, 3L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_COMMENT_POST);
    }

    @Test
    @DisplayName("이미 삭제된 댓글이면 예외를 던진다")
    void delete_alreadyDeleted_shouldThrow() {
        PostComment comment = mockComment(1L, 2L, "content");
        comment.markDeleted();
        given(commentService.findByCommentAndMatchedUserId(1L, 3L)).willReturn(comment);

        assertThatThrownBy(() -> deleteCommentService.delete(10L, 2L, 1L, 3L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED_POST);
    }

    @Test
    @DisplayName("정상 삭제 시 자식까지 삭제하며 히스토리가 기록된다")
    void delete_success() {
        PostComment comment = mockComment(1L, 2L, "content");
        given(commentService.findByCommentAndMatchedUserId(1L, 3L)).willReturn(comment);
        given(commentService.findByParentCommentId(anyLong())).willReturn(Collections.emptyList());

        Long result = deleteCommentService.delete(10L, 2L, 1L, 3L);

        assertThat(result).isEqualTo(2L);
        assertThat(comment.isDeleted()).isTrue();
        verify(historyRecorder).recordHistory(
                eq(HistoryType.POST_COMMENT),
                eq(1L),
                eq(ActionType.DELETE),
                eq(CommentHistorySnapshot.from(comment))
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
