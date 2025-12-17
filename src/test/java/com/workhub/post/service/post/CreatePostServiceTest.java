package com.workhub.post.service.post;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.post.request.PostRequest;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostType;
import com.workhub.post.service.PostValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePostServiceTest {

    @Mock
    private PostService postService;

    @Mock
    private PostValidator postValidator;

    @Mock
    private HistoryRecorder historyRecorder;

    @Mock
    private PostNotificationService postNotificationService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private CreatePostService createPostService;

    private List<MultipartFile> files;

    @BeforeEach
    void setUp() {
        files = List.of();
    }

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다")
    void create_withMissingParent_shouldThrow() {
        // given
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1", 1L, List.of()
        );
        when(postService.existsActivePost(1L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request, files))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARENT_POST_NOT_FOUND);

        verify(postService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("부모 게시글 노드가 다르면 예외를 던진다")
    void create_withMismatchedParentNode_shouldThrow() {
        // given
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "11.1.1", 1L, List.of()
        );
        when(postService.existsActivePost(1L)).thenReturn(true);
        Post parent = Post.builder().postId(1L).projectNodeId(99L).build();
        when(postService.findById(1L)).thenReturn(parent);
        doThrow(new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_POST))
                .when(postService).validateNode(parent, 20L);

        // when & then
        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request, files))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_PROJECT_POST);
    }

    @Test
    @DisplayName("파일 없이 게시글 생성 시 빈 파일 목록으로 반환한다")
    void create_withoutFiles_returnsEmptyFiles() {
        // given
        Post saved = Post.builder()
                .postId(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .projectNodeId(20L)
                .userId(30L)
                .build();
        when(postService.save(any(Post.class))).thenReturn(saved);

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of()
        );

        // when
        PostResponse result = createPostService.create(10L, 20L, 30L, request, files);

        // then
        assertThat(result.postId()).isEqualTo(10L);
        assertThat(result.files()).isEmpty();
        verify(fileService, never()).uploadFiles(any());
        verify(historyRecorder).recordHistory(eq(HistoryType.POST), eq(10L), eq(ActionType.CREATE), any(Object.class));
        verify(postNotificationService).notifyCreated(10L, saved);
    }

    @Test
    @DisplayName("첨부 파일과 함께 게시글을 생성하면 업로드 및 저장 흐름이 호출된다")
    void create_withFiles_shouldUploadAndPersist() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "hello".getBytes()
        );
        files = List.of(file);

        Post saved = Post.builder()
                .postId(11L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .projectNodeId(20L)
                .userId(30L)
                .build();
        when(postService.save(any(Post.class))).thenReturn(saved);

        List<FileUploadResponse> uploads = List.of(FileUploadResponse.from("uploaded.txt", "hello.txt", ""));
        when(fileService.uploadFiles(files)).thenReturn(uploads);

        // return exactly the list passed to savePostFiles to reflect persistence
        when(postService.savePostFiles(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of()
        );

        // when
        PostResponse response = createPostService.create(10L, 20L, 30L, request, files);

        // then
        assertThat(response.postId()).isEqualTo(11L);
        assertThat(response.files()).hasSize(1);
        assertThat(response.files().get(0).fileName()).isEqualTo("uploaded.txt");

        verify(fileService).uploadFiles(files);

        ArgumentCaptor<List<PostFile>> fileCaptor = ArgumentCaptor.forClass(List.class);
        verify(postService).savePostFiles(fileCaptor.capture());
        assertThat(fileCaptor.getValue().get(0).getFileOrder()).isEqualTo(1);

        verify(historyRecorder).recordHistory(eq(HistoryType.POST), eq(11L), eq(ActionType.CREATE), any(Object.class));
        verify(postNotificationService).notifyCreated(10L, saved);
    }

    @Test
    @DisplayName("프로젝트 상태가 유효하지 않으면 게시글을 생성할 수 없다")
    void create_withInvalidProjectStatus_shouldThrow() {
        // given
        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, List.of()
        );
        doThrow(new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST))
                .when(postValidator).validateNodeAndProject(20L, 10L);

        // when & then
        assertThatThrownBy(() -> createPostService.create(10L, 20L, 30L, request, files))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);

        verify(postService, never()).save(any(Post.class));
    }
}
