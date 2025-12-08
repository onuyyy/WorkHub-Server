package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.post.repository.PostFileRepository;
import com.workhub.post.repository.PostLinkRepository;
import com.workhub.post.repository.PostRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(SpringExtension.class)
public class PostServiceTest {
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
    CreatePostService createPostService;
    UpdatePostService updatePostService;
    DeletePostService deletePostService;

    @BeforeEach
    void setUp() {
        createPostService = new CreatePostService(postService, projectService);
        updatePostService = new UpdatePostService(postService, projectService);
        deletePostService = new DeletePostService(postService, projectService);
        given(postRepository.findByParentPostIdAndDeletedAtIsNull(anyLong())).willReturn(Collections.emptyList());
        given(postFileRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
        given(postLinkRepository.findByPostId(anyLong())).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다.")
    void create_withParentNotFound_shouldThrow() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1",1L, List.of(), List.of()
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);
        given(projectService.validateProject(10L)).willReturn(mockProject(10L));

        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);
    }

    @Test
    @DisplayName("부모 게시글이 이미 삭제된 경우 예외를 던진다")
    void create_withDeletedParent_shouldThrowAlreadyDeleted() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1",1L, List.of(), List.of()
        );
        given(postRepository.existsByPostIdAndDeletedAtIsNull(1L)).willReturn(false);
        given(projectService.validateProject(10L)).willReturn(mockProject(10L));

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
        given(projectService.validateProject(10L)).willReturn(mockProject(10L));

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of(), List.of()
        );

        PostResponse result = createPostService.create(10L, 20L, 30L, request);

        assertThat(result.postId()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("title");
    }

    @Test
    @DisplayName("프로젝트 상태가 유효하지 않으면 게시글을 생성할 수 없다")
    void create_withInvalidProjectStatus_shouldThrow() {
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of(), List.of()
        );
        given(projectService.validateProject(10L)).willThrow(new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST));

        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
    }

    @Test
    @DisplayName("게시글을 수정하면 필드가 갱신된다")
    void update_success_shouldChangeFields() {
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

        given(projectService.validateProject(10L)).willReturn(mockProject(10L));
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(origin));

        PostResponse result = updatePostService.update(10L, 20L, 1L, 30L, request);

        assertThat(result.title()).isEqualTo("new");
        assertThat(result.postIp()).isEqualTo("2.2.2.2");
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

        given(projectService.validateProject(10L)).willReturn(mockProject(10L));
        given(postRepository.findByPostIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> updatePostService.update(10L, 20L, 1L, 99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_POST_UPDATE);
    }

    @Test
    @DisplayName("프로젝트 상태가 유효하지 않으면 게시글을 수정할 수 없다")
    void update_withInvalidProjectStatus_shouldThrow() {
        PostUpdateRequest request = new PostUpdateRequest(
                "new", PostType.NOTICE, "new content", "2.2.2.2", List.of(), List.of()
        );
        given(projectService.validateProject(10L)).willThrow(new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST));

        assertThatThrownBy(() -> updatePostService.update(10L, 20L, 1L, 30L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
    }

    @Test
    @DisplayName("게시글 조회 시 존재하지 않으면 예외를 던진다")
    void findById_withPostNotFound_shouldThrow() {
        given(postRepository.findByPostIdAndDeletedAtIsNull(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
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
    @DisplayName("게시글 삭제 성공 시 repository.delete가 호출된다")
    void delete_success_shouldInvokeDelete() {
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
    }

    @Test
    @DisplayName("작성자가 아니면 게시글을 삭제할 수 없다")
    void delete_withDifferentUser_shouldThrowForbidden() {
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

        assertThatThrownBy(() -> deletePostService.delete(10L, 20L, 1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_POST_DELETE);
    }

    @Test
    @DisplayName("프로젝트 상태가 유효하지 않으면 게시글을 삭제할 수 없다")
    void delete_withInvalidProjectStatus_shouldThrow() {
        given(projectService.validateProject(10L)).willThrow(new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST));

        assertThatThrownBy(() -> deletePostService.delete(10L, 20L, 1L, 30L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
    }

    private Project mockProject(Long projectId) {
        return Project.builder()
                .projectId(projectId)
                .projectTitle("project")
                .status(Status.IN_PROGRESS)
                .build();
    }

}
