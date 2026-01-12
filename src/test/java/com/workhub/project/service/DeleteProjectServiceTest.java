package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DeleteProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private DeleteProjectService deleteProjectService;

    private Project mockProject;

    @BeforeEach
    void init() {
        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("테스트 프로젝트")
                .projectDescription("테스트 설명")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusMonths(6))
                .clientCompanyId(100L)
                .build();
    }

    @Test
    @DisplayName("유효한 프로젝트 ID로 삭제하면 프로젝트가 삭제되고 히스토리가 기록된다.")
    void givenValidProjectId_whenDeleteProject_thenProjectIsDeletedAndHistoryRecorded() {
        // given
        Long projectId = 1L;
        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(
                any(HistoryType.class),
                anyLong(),
                any(ActionType.class),
                any(Object.class)
        );

        // when
        deleteProjectService.deleteProject(projectId);

        // then
        verify(projectService).findProjectById(projectId);
        assertThat(mockProject.getStatus()).isEqualTo(Status.DELETED);

        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.status()).isEqualTo(Status.IN_PROGRESS);
        assertThat(capturedSnapshot.projectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("CONTRACT 상태의 프로젝트를 삭제하면 상태가 DELETED로 변경되고 히스토리가 기록된다.")
    void givenContractProject_whenDeleteProject_thenProjectIsDeletedAndHistoryRecorded() {
        // given
        Long projectId = 1L;
        Project contractProject = Project.builder()
                .projectId(projectId)
                .projectTitle("계약 중인 프로젝트")
                .status(Status.CONTRACT)
                .build();

        when(projectService.findProjectById(projectId)).thenReturn(contractProject);
        lenient().doNothing().when(historyRecorder).recordHistory(
                any(HistoryType.class),
                anyLong(),
                any(ActionType.class),
                any(Object.class)
        );

        // when
        deleteProjectService.deleteProject(projectId);

        // then
        verify(projectService).findProjectById(projectId);
        assertThat(contractProject.getStatus()).isEqualTo(Status.DELETED);

        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.status()).isEqualTo(Status.CONTRACT);
    }

    @Test
    @DisplayName("COMPLETED 상태의 프로젝트를 삭제하면 상태가 DELETED로 변경되고 히스토리가 기록된다.")
    void givenCompletedProject_whenDeleteProject_thenProjectIsDeletedAndHistoryRecorded() {
        // given
        Long projectId = 1L;
        Project completedProject = Project.builder()
                .projectId(projectId)
                .projectTitle("완료된 프로젝트")
                .status(Status.COMPLETED)
                .build();

        when(projectService.findProjectById(projectId)).thenReturn(completedProject);
        lenient().doNothing().when(historyRecorder).recordHistory(
                any(HistoryType.class),
                anyLong(),
                any(ActionType.class),
                any(Object.class)
        );

        // when
        deleteProjectService.deleteProject(projectId);

        // then
        verify(projectService).findProjectById(projectId);
        assertThat(completedProject.getStatus()).isEqualTo(Status.DELETED);

        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.status()).isEqualTo(Status.COMPLETED);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 삭제하면 예외를 발생시킨다.")
    void givenInvalidProjectId_whenDeleteProject_thenThrowException() {
        // given
        Long projectId = 999L;
        when(projectService.findProjectById(projectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> deleteProjectService.deleteProject(projectId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectService).findProjectById(projectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 삭제 시 히스토리는 삭제 전 상태를 기록한다.")
    void givenProject_whenDeleteProject_thenHistoryRecordsBeforeStatus() {
        // given
        Long projectId = 1L;
        Status beforeStatus = mockProject.getStatus();
        when(projectService.findProjectById(projectId)).thenReturn(mockProject);
        lenient().doNothing().when(historyRecorder).recordHistory(
                any(HistoryType.class),
                anyLong(),
                any(ActionType.class),
                any(Object.class)
        );

        // when
        deleteProjectService.deleteProject(projectId);

        // then
        ArgumentCaptor<ProjectHistorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(ProjectHistorySnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(projectId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        ProjectHistorySnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.status()).isEqualTo(beforeStatus);
    }
}