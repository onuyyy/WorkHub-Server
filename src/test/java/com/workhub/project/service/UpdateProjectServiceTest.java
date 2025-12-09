package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.dto.request.UpdateStatusRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.userTable.entity.UserTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static com.workhub.userTable.entity.UserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UpdateProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private UpdateProjectService updateProjectService;

    private Project mockProject;
    private CreateProjectRequest updateRequest;

    @BeforeEach
    void init() {
        // SecurityContext 설정
        UserTable mockUser = UserTable.builder()
                .userId(1L)
                .loginId("testuser")
                .password("password")
                .role(ADMIN)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("기존 프로젝트")
                .projectDescription("기존 설명")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        updateRequest = new CreateProjectRequest(
                "수정된 프로젝트",
                "수정된 설명",
                200L,
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 11, 30)
        );
    }

    @Test
    @DisplayName("프로젝트 상태를 정상적으로 업데이트하고 히스토리를 기록한다")
    void givenValidProjectIdAndStatus_whenUpdateProjectStatus_thenSuccess() {
        // given
        Long projectId = 1L;
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.COMPLETED);

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        updateProjectService.updateProjectStatus(projectId, statusRequest);

        // then
        verify(projectService).findProjectById(projectId);

        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.status()).isEqualTo(Status.IN_PROGRESS);
        assertThat(capturedSnapshot.projectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 상태 업데이트 시 예외 발생")
    void givenInvalidProjectId_whenUpdateProjectStatus_thenThrowException() {
        // given
        Long invalidProjectId = 999L;
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.COMPLETED);

        when(projectService.findProjectById(invalidProjectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() ->
                updateProjectService.updateProjectStatus(invalidProjectId, statusRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectService).findProjectById(invalidProjectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 정보를 정상적으로 업데이트하고 변경 전 상태를 히스토리에 기록한다")
    void givenValidUpdateRequest_whenUpdateProject_thenSuccessAndRecordHistory() {
        // given
        Long projectId = 1L;

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectTitle()).isEqualTo("수정된 프로젝트");
        assertThat(result.projectDescription()).isEqualTo("수정된 설명");
        assertThat(result.contractStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.contractEndDate()).isEqualTo(LocalDate.of(2024, 11, 30));

        assertThat(mockProject.getProjectTitle()).isEqualTo("수정된 프로젝트");
        assertThat(mockProject.getProjectDescription()).isEqualTo("수정된 설명");
        assertThat(mockProject.getContractStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(mockProject.getContractEndDate()).isEqualTo(LocalDate.of(2024, 11, 30));
        assertThat(mockProject.getClientCompanyId()).isEqualTo(200L);

        verify(projectService).findProjectById(projectId);

        // 변경 전 전체 스냅샷을 한 번만 히스토리에 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectTitle()).isEqualTo("기존 프로젝트");
        assertThat(capturedSnapshot.projectDescription()).isEqualTo("기존 설명");
        assertThat(capturedSnapshot.status()).isEqualTo(Status.IN_PROGRESS);
        assertThat(capturedSnapshot.contractStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(capturedSnapshot.contractEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(capturedSnapshot.company()).isEqualTo(100L);
    }

    @Test
    @DisplayName("일부 필드만 변경 시에도 변경 전 전체 상태를 히스토리에 기록한다")
    void givenPartialUpdateRequest_whenUpdateProject_thenRecordCompleteSnapshot() {
        // given
        Long projectId = 1L;

        // 제목과 설명만 변경하는 요청 (다른 필드는 기존 값과 동일)
        CreateProjectRequest partialRequest = new CreateProjectRequest(
                "수정된 프로젝트",
                "수정된 설명",
                100L, // 기존과 동일
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 1, 1), // 기존과 동일
                LocalDate.of(2024, 12, 31) // 기존과 동일
        );

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, partialRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectTitle()).isEqualTo("수정된 프로젝트");
        assertThat(result.projectDescription()).isEqualTo("수정된 설명");

        verify(projectService).findProjectById(projectId);

        // 변경 전 전체 스냅샷을 한 번만 히스토리에 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectTitle()).isEqualTo("기존 프로젝트");
        assertThat(capturedSnapshot.projectDescription()).isEqualTo("기존 설명");
    }

    @Test
    @DisplayName("필드 변경이 없어도 히스토리가 기록된다")
    void givenNoChanges_whenUpdateProject_thenHistoryStillRecorded() {
        // given
        Long projectId = 1L;

        // 모든 필드가 기존 값과 동일한 요청
        CreateProjectRequest noChangeRequest = new CreateProjectRequest(
                "기존 프로젝트",
                "기존 설명",
                100L,
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, noChangeRequest);

        // then
        assertThat(result).isNotNull();

        verify(projectService).findProjectById(projectId);

        // 실제 구현은 변경 여부와 무관하게 항상 히스토리 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectTitle()).isEqualTo("기존 프로젝트");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 업데이트 시 예외 발생")
    void givenInvalidProjectId_whenUpdateProject_thenThrowException() {
        // given
        Long invalidProjectId = 999L;

        when(projectService.findProjectById(invalidProjectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() ->
                updateProjectService.updateProject(invalidProjectId, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectService).findProjectById(invalidProjectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 제목만 변경 시에도 변경 전 전체 상태를 히스토리에 기록한다")
    void givenOnlyTitleChange_whenUpdateProject_thenRecordCompleteSnapshot() {
        // given
        Long projectId = 1L;

        CreateProjectRequest titleOnlyRequest = new CreateProjectRequest(
                "새로운 제목",
                "기존 설명",
                100L,
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, titleOnlyRequest);

        // then
        assertThat(result.projectTitle()).isEqualTo("새로운 제목");
        assertThat(mockProject.getProjectTitle()).isEqualTo("새로운 제목");

        verify(projectService).findProjectById(projectId);

        // 변경 전 전체 스냅샷을 한 번만 히스토리에 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectTitle()).isEqualTo("기존 프로젝트");
    }

    @Test
    @DisplayName("계약 기간만 변경 시에도 변경 전 전체 상태를 히스토리에 기록한다")
    void givenOnlyDateChange_whenUpdateProject_thenRecordCompleteSnapshot() {
        // given
        Long projectId = 1L;

        CreateProjectRequest dateOnlyRequest = new CreateProjectRequest(
                "기존 프로젝트",
                "기존 설명",
                100L,
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 10, 31)
        );

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, dateOnlyRequest);

        // then
        assertThat(mockProject.getContractStartDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(mockProject.getContractEndDate()).isEqualTo(LocalDate.of(2024, 10, 31));

        verify(projectService).findProjectById(projectId);

        // 변경 전 전체 스냅샷을 한 번만 히스토리에 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.contractStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(capturedSnapshot.contractEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("고객사만 변경 시에도 변경 전 전체 상태를 히스토리에 기록한다")
    void givenOnlyCompanyChange_whenUpdateProject_thenRecordCompleteSnapshot() {
        // given
        Long projectId = 1L;

        CreateProjectRequest companyOnlyRequest = new CreateProjectRequest(
                "기존 프로젝트",
                "기존 설명",
                300L, // 변경
                List.of(1L, 2L),
                List.of(3L, 4L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, companyOnlyRequest);

        // then
        assertThat(mockProject.getClientCompanyId()).isEqualTo(300L);

        verify(projectService).findProjectById(projectId);

        // 변경 전 전체 스냅샷을 한 번만 히스토리에 기록
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder, times(1)).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectTitle()).isEqualTo("기존 프로젝트");
        assertThat(capturedSnapshot.company()).isEqualTo(100L); // 변경 전 고객사 ID
    }

    @Test
    @DisplayName("프로젝트 업데이트 시 모든 메서드가 순차적으로 호출된다")
    void givenUpdateRequest_whenUpdateProject_thenAllMethodsCalledInOrder() {
        // given
        Long projectId = 1L;

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        updateProjectService.updateProject(projectId, updateRequest);

        // then
        var inOrder = inOrder(projectService, historyRecorder);
        inOrder.verify(projectService).findProjectById(projectId);
        inOrder.verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }

    @Test
    @DisplayName("프로젝트 업데이트 후 반환된 응답에 모든 필드가 포함된다")
    void givenUpdateRequest_whenUpdateProject_thenReturnCompleteResponse() {
        // given
        Long projectId = 1L;

        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        ProjectResponse result = updateProjectService.updateProject(projectId, updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectId()).isEqualTo(projectId);
        assertThat(result.projectTitle()).isEqualTo("수정된 프로젝트");
        assertThat(result.projectDescription()).isEqualTo("수정된 설명");
        assertThat(result.status()).isEqualTo(Status.IN_PROGRESS);
        assertThat(result.contractStartDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.contractEndDate()).isEqualTo(LocalDate.of(2024, 11, 30));
    }
}