package com.workhub.post.service.post;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.dto.post.request.PostUpdateRequest;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.repository.post.PostFileRepository;
import com.workhub.post.repository.post.PostLinkRepository;
import com.workhub.post.repository.post.PostRepository;
import com.workhub.post.service.PostValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class UpdatePostServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    PostFileRepository postFileRepository;
    @Mock
    PostLinkRepository postLinkRepository;
    @InjectMocks
    PostService postService;
    @Mock
    PostValidator postValidator;
    @Mock
    HistoryRecorder historyRecorder;
    @Mock
    PostNotificationService postNotificationService;

    UpdatePostService updatePostService;

    @BeforeEach
    void setUp() {
        updatePostService = new UpdatePostService(postService, postValidator, historyRecorder, postNotificationService);
        given(postRepository.findByParentPostIdAndDeletedAtIsNull(anyLong())).willReturn(Collections.emptyList());
        given(postFileRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        given(postLinkRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        willDoNothing().given(postValidator).validateNodeAndProject(anyLong(), anyLong());
    }

    @Test
    @DisplayName("게시글을 수정하면 필드가 갱신되고 히스토리가 기록된다")
    void update_success_shouldChangeFields_andRecordHistory() {
        Post origin = Post.builder()
                .postId(1L)
                .title("old")
                .content("old")
                .type(PostType.GENERAL)
                .postIp("1.1.1.1")
                .projectNodeId(20L)
                .userId(30L)
                .build();

        PostUpdateRequest request = new PostUpdateRequest(
                "new", PostType.NOTICE, "new content", "2.2.2.2", List.of(), List.of()
        );

        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(origin));

        PostResponse result = updatePostService.update(10L, 20L, 1L, 30L, request);

        assertThat(result.title()).isEqualTo("new");
        assertThat(result.postIp()).isEqualTo("2.2.2.2");
        verify(historyRecorder).recordHistory(
                eq(com.workhub.global.entity.HistoryType.POST),
                eq(1L),
                eq(com.workhub.global.entity.ActionType.UPDATE),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("작성자가 아니면 게시글을 수정할 수 없다")
    void update_withDifferentUser_shouldThrow() {
        Post origin = Post.builder()
                .postId(1L)
                .title("old")
                .content("old")
                .type(PostType.GENERAL)
                .postIp("1.1.1.1")
                .projectNodeId(20L)
                .userId(30L)
                .build();

        PostUpdateRequest request = new PostUpdateRequest(
                "new", PostType.NOTICE, "new content", "2.2.2.2", List.of(), List.of()
        );

        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> updatePostService.update(10L, 20L, 1L, 99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_POST_UPDATE);
    }
}
