package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostFileRequest;
import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.port.AuthorLookupPort;
import com.workhub.cs.port.dto.AuthorProfile;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    }

    @Test
    @DisplayName(" CS 게시글을 작성하면 작성한 게시글 정보를 보여준다.")
    void givenCsPostCreateRequest_whenCreate_thenSuccess() {
        Long projectId = 1L;
        Long userId = 2L;
        CsPostRequest request = new CsPostRequest("문의 제목", "문의 내용", null);

        when(projectService.validateCompletedProject(projectId)).thenReturn(Project.builder().projectId(projectId).projectTitle("p").status(Status.COMPLETED).build());
        when(csPostService.save(any(CsPost.class))).thenReturn(mockSaved);
        when(authorLookupPort.findByUserId(userId)).thenReturn(Optional.of(new AuthorProfile(userId, "작성자")));

        CsPostResponse result = createCsPostService.create(projectId, userId, request);

        assertThat(result.csPostId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("문의 제목");
        assertThat(result.content()).isEqualTo("문의 내용");
        assertThat(result.userName()).isEqualTo("작성자");

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).save(any(CsPost.class));
        verify(csPostService, never()).saveAllFiles(anyList());
    }

    @Test
    @DisplayName("파일이 포함된 게시글 작성 시 파일도 저장된다.")
    void givenRequestWithFiles_whenCreate_thenFilesAreSaved() {
        Long projectId = 1L;
        Long userId = 2L;

        List<CsPostFileRequest> fileRequests = Arrays.asList(
                new CsPostFileRequest("file1", 1),
                new CsPostFileRequest("file2", 2)
        );

        CsPostRequest request = new CsPostRequest("문의 제목", "내용", fileRequests);

        when(projectService.validateCompletedProject(projectId)).thenReturn(Project.builder().projectId(projectId).projectTitle("p").status(Status.COMPLETED).build());
        when(csPostService.save(any(CsPost.class))).thenReturn(mockSaved);
        when(csPostService.saveAllFiles(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorLookupPort.findByUserId(userId)).thenReturn(Optional.of(new AuthorProfile(userId, "작성자")));

        createCsPostService.create(projectId, userId, request);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).save(any(CsPost.class));
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
        when(authorLookupPort.findByUserId(userId)).thenReturn(Optional.of(new AuthorProfile(userId, "작성자")));

        createCsPostService.create(projectId, userId, request);

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

        assertThatThrownBy(() -> createCsPostService.create(projectId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);

        verify(csPostService, never()).save(any(CsPost.class));
    }
}
