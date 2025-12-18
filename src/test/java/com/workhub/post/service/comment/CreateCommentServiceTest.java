package com.workhub.post.service.comment;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.comment.CommentHistorySnapshot;
import com.workhub.post.dto.comment.request.CommentRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.event.CommentCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateCommentServiceTest {

    @Mock
    CommentService commentService;
    @Mock
    HistoryRecorder historyRecorder;
    @Mock
    PostValidator postValidator;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CreateCommentService createCommentService;

    @BeforeEach
    void setUp() {
        given(postValidator.validatePostToProject(anyLong(), anyLong()))
                .willReturn(Post.builder().build());
    }

    @Test
    @DisplayName("내용이 비어 있으면 예외를 던진다")
    void create_withBlankContent_shouldThrow() {
        CommentRequest request = new CommentRequest("   ", null);

        assertThatThrownBy(() -> createCommentService.create(1L, 2L, 3L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_COMMENT_CONTENT);
    }

    @Test
    @DisplayName("부모 댓글의 게시글이 다르면 예외를 던진다")
    void create_withMismatchedParent_shouldThrow() {
        CommentRequest request = new CommentRequest("hello", 99L);
        // resolveParent가 현재 postId로 조회하므로 postId와 다른 게시글을 리턴시켜 불일치 유발
        given(commentService.findById(99L)).willReturn(mockComment(5L, 10L, null, "parent"));

        assertThatThrownBy(() -> createCommentService.create(1L, 2L, 3L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_COMMENT_POST);
    }

    @Test
    @DisplayName("정상 생성 시 댓글과 히스토리를 반환/기록한다")
    void create_success() {
        CommentRequest request = new CommentRequest("hello", null);
        PostComment saved = mockComment(10L, 2L, null, "hello");
        given(commentService.save(any(PostComment.class))).willReturn(saved);

        CommentResponse response = createCommentService.create(1L, 2L, 3L, request);

        assertThat(response.commentId()).isEqualTo(10L);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.POST_COMMENT),
                eq(10L),
                eq(ActionType.CREATE),
                eq(CommentHistorySnapshot.from(saved))
        );
        verify(eventPublisher).publishEvent(any(CommentCreatedEvent.class));
    }

    private PostComment mockComment(Long commentId, Long postId, Long parentId, String content) {
        return PostComment.builder()
                .commentId(commentId)
                .postId(postId)
                .parentCommentId(parentId)
                .userId(3L)
                .content(content)
                .build();
    }
}
