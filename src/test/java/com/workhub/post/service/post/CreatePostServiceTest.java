package com.workhub.post.service.post;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.dto.post.request.PostRequest;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class CreatePostServiceTest {

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

    CreatePostService createPostService;

    @BeforeEach
    void setUp() {
        createPostService = new CreatePostService(postService, postValidator, historyRecorder);
        willDoNothing().given(postValidator).validateNodeAndProject(anyLong(), anyLong());
        given(postRepository.findByParentPostIdAndDeletedAtIsNull(anyLong())).willReturn(Collections.emptyList());
        given(postFileRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        given(postLinkRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다.")
    void create_withParentNotFound_shouldThrow() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1", 1L, List.of(), List.of()
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);

        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);
    }

    @Test
    @DisplayName("부모 게시글이 이미 삭제된 경우 예외를 던진다")
    void create_withDeletedParent_shouldThrowAlreadyDeleted() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1", 1L, List.of(), List.of()
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);

        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);
    }

    @Test
    @DisplayName("정상적으로 게시글을 생성하면 저장된 엔티티를 반환한다")
    void create_success_shouldReturnSavedPost() {
        Post saved = Post.builder()
                .postId(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .build();
        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of(), List.of()
        );

        PostResponse result = createPostService.create(10L, 20L, 30L, request);

        assertThat(result.postId()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("title");
        verify(historyRecorder).recordHistory(
                eq(com.workhub.global.entity.HistoryType.POST),
                eq(10L),
                eq(com.workhub.global.entity.ActionType.CREATE),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("프로젝트 상태가 유효하지 않으면 게시글을 생성할 수 없다")
    void create_withInvalidProjectStatus_shouldThrow() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of(), List.of()
        );
        willDoNothing().given(postValidator).validateNodeAndProject(anyLong(), anyLong());
        willThrow(new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST))
                .given(postValidator).validateNodeAndProject(20L, 10L);

        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
    }
}
