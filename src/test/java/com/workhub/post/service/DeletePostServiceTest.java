package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.repository.post.PostFileRepository;
import com.workhub.post.repository.post.PostLinkRepository;
import com.workhub.post.repository.post.PostRepository;
import com.workhub.post.service.post.DeletePostService;
import com.workhub.post.service.post.PostService;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class DeletePostServiceTest {

    @Mock
    PostRepository postRepository;
    @Mock
    PostFileRepository postFileRepository;
    @Mock
    PostLinkRepository postLinkRepository;
    @InjectMocks
    PostService postService;
    @Mock
    ProjectService projectService;
    @Mock
    HistoryRecorder historyRecorder;

    DeletePostService deletePostService;

    @BeforeEach
    void setUp() {
        deletePostService = new DeletePostService(postService, projectService, historyRecorder);
        given(postRepository.findByParentPostIdAndDeletedAtIsNull(anyLong())).willReturn(Collections.emptyList());
        given(postFileRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        given(postLinkRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("삭제 대상 게시글이 없으면 예외를 던진다")
    void delete_withPostNotFound_shouldThrow() {
        given(postRepository.findByPostIdAndDeletedAtIsNull(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> deletePostService.delete(10L, 20L, 99L, 30L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 삭제하려 하면 예외를 던진다")
    void delete_withAlreadyDeletedPost_shouldThrow() {
        Post deleted = Post.builder()
                .postId(1L)
                .type(PostType.NOTICE)
                .title("title")
                .content("content")
                .projectNodeId(20L)
                .userId(30L)
                .build();
        deleted.markDeleted();
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> deletePostService.delete(10L, 20L, 1L, 30L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED_POST);
    }

    @Test
    @DisplayName("게시글 삭제 성공 시 히스토리가 기록되고 소프트 삭제된다")
    void delete_success_shouldMarkDeletedAndRecordHistory() {
        Post existing = Post.builder()
                .postId(1L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .projectNodeId(20L)
                .userId(30L)
                .build();
        given(projectService.validateProject(10L)).willReturn(mockProject(10L));
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(existing));

        deletePostService.delete(10L, 20L, 1L, 30L);

        assertThat(existing.isDeleted()).isTrue();
        verify(historyRecorder).recordHistory(
                eq(com.workhub.global.entity.HistoryType.POST),
                eq(1L),
                eq(com.workhub.global.entity.ActionType.DELETE),
                any(Object.class)
        );
    }

    private Project mockProject(Long projectId) {
        return Project.builder()
                .projectId(projectId)
                .projectTitle("project")
                .status(Status.IN_PROGRESS)
                .build();
    }
}
