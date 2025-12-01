package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostStatus;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCsPostServiceTest {

    @Mock
    private CsPostService csPostService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private UpdateCsPostService updateCsPostService;

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
    @DisplayName("CS 게시글 수정 시 정상 저장되는지 검증한다.")
    void givenCsPostUpdateRequest_whenUpdate_thenSuccess() {

        Long projectId = 1L;
        Long csPostId = 2L;
        Long userId = 2L;

        mockProjectLookup(projectId);

        CsPost original = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(userId)
                .title("원래 제목")
                .content("원래 내용")
                .build();

        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusMinutes(1);
        ReflectionTestUtils.setField(original, "updatedAt", originalUpdatedAt);

        CsPostUpdateRequest request =
                new CsPostUpdateRequest("수정 제목", "수정 완료", List.of());

        when(csPostService.findById(csPostId))
                .thenReturn(original);

        when(csPostService.findFilesByCsPostId(csPostId))
                .thenReturn(List.of());

        CsPostResponse result = updateCsPostService.update(projectId, csPostId, userId, request);

        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 완료");

        assertThat(original.getTitle()).isEqualTo("수정 제목");
        assertThat(original.getContent()).isEqualTo("수정 완료");

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService, never()).save(any(CsPost.class));
    }

    @Test
    @DisplayName("완료되지 않은 프로젝트에서는 CS 게시글을 수정할 수 없다.")
    void givenNotCompletedProject_whenUpdate_thenThrow() {
        Long projectId = 1L;
        Long csPostId = 1L;
        Long userId = 2L;

        CsPostUpdateRequest request = new CsPostUpdateRequest("수정 제목", "수정 완료", List.of());

        mockProjectLookup(projectId, Status.IN_PROGRESS);

        assertThatThrownBy(() -> updateCsPostService.update(projectId, csPostId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);

        verify(csPostService, never()).findById(any());
        verify(csPostService, never()).save(any(CsPost.class));
    }

    @Test
    @DisplayName("게시글 작성자가 아닌 사용자가 수정하려고 하면 FORBIDDEN_CS_POST_UPDATE 예외가 발생한다.")
    void givenDifferentUser_whenUpdate_thenThrowForbidden() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long authorId = 2L;
        Long requesterId = 3L;

        mockProjectLookup(projectId);

        CsPost original = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(authorId)
                .title("원래 제목")
                .content("원래 내용")
                .build();

        CsPostUpdateRequest request = new CsPostUpdateRequest("수정 제목", "수정 완료", List.of());

        when(csPostService.findById(csPostId))
                .thenReturn(original);

        assertThatThrownBy(() -> updateCsPostService.update(projectId, csPostId, requesterId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_CS_POST_UPDATE);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService, never()).save(any(CsPost.class));

    }

    @Test
    @DisplayName("CS 게시글의 진행 상태를 변경한다.")
    void givenStatus_whenStatusUpdate_thenSuccess() {
        CsPostStatus status = CsPostStatus.RECEIVED;

        Long projectId = 1L;
        CsPost mockSaved = CsPost.builder()
                .csPostId(1L)
                .title("title")
                .content("content")
                .projectId(1L)
                .userId(1L)
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();

        mockProjectLookup(projectId);
        when(csPostService.findById(1L)).thenReturn(mockSaved);

        updateCsPostService.changeStatus(projectId, 1L, status);

        assertThat(mockSaved.getCsPostStatus()).isEqualTo(status);
        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(1L);
        verify(csPostService, never()).save(any(CsPost.class));
    }

    @Test
    @DisplayName("존재하지 않는 CS 게시글 상태 변경 시 예외 발생")
    void givenInvalidPostId_whenStatusUpdate_thenThrowException() {
        Long projectId = 1L;
        mockProjectLookup(projectId);
        when(csPostService.findById(1L)).thenThrow(new BusinessException(ErrorCode.NOT_EXISTS_CS_POST));

        assertThatThrownBy(() -> updateCsPostService.changeStatus(projectId, 1L, CsPostStatus.RECEIVED))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXISTS_CS_POST.getMessage());

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(1L);
        verify(csPostService, never()).save(any(CsPost.class));
    }

    @Test
    @DisplayName("상태를 정상 변경하면 변경된 상태를 반환한다.")
    void givenStatus_whenChangeStatus_thenReturnUpdatedStatus() {
        Long projectId = 1L;
        CsPost mockSaved = CsPost.builder()
                .csPostId(1L)
                .title("title")
                .content("content")
                .projectId(1L)
                .userId(1L)
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();

        mockProjectLookup(projectId);
        when(csPostService.findById(1L)).thenReturn(mockSaved);

        CsPostStatus result = updateCsPostService.changeStatus(projectId, 1L, CsPostStatus.COMPLETED);

        assertThat(result).isEqualTo(CsPostStatus.COMPLETED);
        assertThat(mockSaved.getCsPostStatus()).isEqualTo(CsPostStatus.COMPLETED);
        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(1L);
        verify(csPostService, never()).save(any());
    }

    private void mockProjectLookup(Long projectId) {
        mockProjectLookup(projectId, Status.COMPLETED);
    }

    private void mockProjectLookup(Long projectId, Status status) {
        if (Status.COMPLETED.equals(status)) {
            when(projectService.validateCompletedProject(projectId)).thenReturn(
                    Project.builder()
                            .projectId(projectId)
                            .projectTitle("project")
                            .status(Status.COMPLETED)
                            .build()
            );
            return;
        }

        when(projectService.validateCompletedProject(projectId)).thenThrow(
                new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST)
        );
    }
}
