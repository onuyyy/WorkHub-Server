package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.global.port.AuthorLookupPort;
import com.workhub.global.port.dto.AuthorProfile;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCsPostServiceTest {

    @Mock
    private CsPostService csPostService;

    @Mock
    private ProjectService projectService;

    @Mock
    private CsPostNotificationService csPostNotificationService;

    @Mock
    private FileService fileService;

    @Mock
    private AuthorLookupPort authorLookupPort;

    @InjectMocks
    private CreateCsPostService createCsPostService;

    private CsPost mockSaved;

    @BeforeEach
    void init() {
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .build();

        lenient().when(authorLookupPort.findByUserId(anyLong()))
                .thenReturn(Optional.of(new AuthorProfile(mockSaved.getUserId(), "작성자")));
    }

    @Test
    @DisplayName(" CS 게시글을 작성하면 작성한 게시글 정보를 보여준다.")
    void givenCsPostCreateRequest_whenCreate_thenSuccess() {
        Long projectId = 1L;
        Long userId = 2L;
        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);

        when(projectService.validateCompletedProject(projectId)).thenReturn(Project.builder().projectId(projectId).projectTitle("p").status(Status.COMPLETED).build());
        when(csPostService.save(any(CsPost.class))).thenReturn(mockSaved);

        CsPostResponse result = createCsPostService.create(projectId, userId, request, null);

        assertThat(result.csPostId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("문의 제목");
        assertThat(result.content()).isEqualTo("문의 내용");

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).save(any(CsPost.class));
        verify(csPostService, never()).saveAllFiles(anyList());
    }

    @Test
    @DisplayName("파일이 포함된 게시글 작성 시 파일도 저장된다.")
    void givenRequestWithFiles_whenCreate_thenFilesAreSaved() {
        Long projectId = 1L;
        Long userId = 2L;

        List<MultipartFile> multipartFiles = List.of(
                new MockMultipartFile("files", "file1.png", "image/png", "data1".getBytes()),
                new MockMultipartFile("files", "file2.png", "image/png", "data2".getBytes())
        );

        List<FileUploadResponse> uploadResponses = List.of(
                FileUploadResponse.from("file1.png", "file1.png", "https://example.com/file1"),
                FileUploadResponse.from("file2.png", "file2.png", "https://example.com/file2")
        );

        CsPostRequest request = new CsPostRequest("문의 제목", "내용", null);

        when(projectService.validateCompletedProject(projectId)).thenReturn(Project.builder().projectId(projectId).projectTitle("p").status(Status.COMPLETED).build());
        when(csPostService.save(any(CsPost.class))).thenReturn(mockSaved);
        when(fileService.uploadFiles(multipartFiles)).thenReturn(uploadResponses);
        when(csPostService.saveAllFiles(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        createCsPostService.create(projectId, userId, request, multipartFiles);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).save(any(CsPost.class));
        verify(fileService).uploadFiles(multipartFiles);
        verify(csPostService, times(1)).saveAllFiles(anyList());
    }

    @Test
    @DisplayName("요청 DTO가 매핑되어 엔티티로 저장되는지 검증한다.")
    void givenRequest_whenCreate_thenEntityMappedSuccessfully() {
        Long projectId = 1L;
        Long userId = 2L;

        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);
        when(projectService.validateCompletedProject(projectId)).thenReturn(Project.builder().projectId(projectId).projectTitle("p").status(Status.COMPLETED).build());
        when(csPostService.save(any(CsPost.class))).thenReturn(mockSaved);

        createCsPostService.create(projectId, userId, request, null);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).save(argThat(post ->
                post.getProjectId().equals(projectId) &&
                        post.getUserId().equals(userId) &&
                        post.getTitle().equals("문의 제목") &&
                        post.getContent().equals("문의 내용")
        ));
    }

    @Test
    @DisplayName("완료되지 않은 프로젝트에서는 CS 게시글을 생성할 수 없다.")
    void givenNotCompletedProject_whenCreate_thenThrow() {
        Long projectId = 1L;
        Long userId = 2L;
        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);

        when(projectService.validateCompletedProject(projectId)).thenThrow(
                new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST)
        );

        assertThatThrownBy(() -> createCsPostService.create(projectId, userId, request, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);

        verify(csPostService, never()).save(any(CsPost.class));
    }
}
