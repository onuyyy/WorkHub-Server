package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.UpdateStatusRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectHistory;
import com.workhub.project.entity.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateProjectServiceTest {

    @Mock
    ProjectService projectService;

    @InjectMocks
    UpdateProjectService updateProjectService;

    @Test
    @DisplayName("프로젝트 상태 변경 시 프로젝트 조회, 상태 변경, 히스토리 저장이 모두 실행된다")
    void updateProjectStatus_success_shouldUpdateStatusAndSaveHistory() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.IN_PROGRESS);

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.CONTRACT)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        updateProjectService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.UPDATE),
                eq("CONTRACT"),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("프로젝트 상태가 정확히 변경된다")
    void updateProjectStatus_success_shouldChangeStatusCorrectly() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.COMPLETED);

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.DELIVERY)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        updateProjectService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        // 프로젝트 엔티티의 updateProjectStatus 메서드가 호출되어 상태가 변경됨
        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.UPDATE),
                eq("DELIVERY"),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("히스토리 저장 시 올바른 사용자 정보가 전달된다")
    void updateProjectStatus_success_shouldPassCorrectUserInfo() {
        // given
        Long projectId = 1L;
        Long userId = 999L;
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.MAINTENANCE);

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.DELIVERY)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        updateProjectService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.UPDATE),
                eq("DELIVERY"),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("모든 상태 타입으로 변경이 가능하다")
    void updateProjectStatus_success_shouldSupportAllStatusTypes() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.CONTRACT)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when & then - 각 상태로 변경 테스트
        for (Status status : Status.values()) {
            UpdateStatusRequest statusRequest = new UpdateStatusRequest(status);
            updateProjectService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);
        }

        // 모든 상태에 대해 히스토리가 저장되었는지 확인
        verify(projectService, times(Status.values().length)).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 상태 변경 시 예외가 발생한다")
    void updateProjectStatus_whenProjectNotFound_shouldThrowException() {
        // given
        Long nonExistentProjectId = 999L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.IN_PROGRESS);

        given(projectService.findProjectById(nonExistentProjectId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> updateProjectService.updateProjectStatus(
                nonExistentProjectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        // 프로젝트 조회 실패 시 이후 메서드는 호출되지 않아야 함
        verify(projectService).findProjectById(nonExistentProjectId);
        verify(projectService, never()).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }

    @Test
    @DisplayName("프로젝트 히스토리를 찾을 수 없는 경우 예외가 발생한다")
    void updateProjectStatus_whenProjectHistoryNotFound_shouldThrowException() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.IN_PROGRESS);

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.CONTRACT)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // updateProjectHistory 내부에서 getProjectOriginalCreator가 호출되고 예외 발생
        doThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND))
                .when(projectService).updateProjectHistory(
                        anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
                );

        // when & then
        assertThatThrownBy(() -> updateProjectService.updateProjectStatus(
                projectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_NOT_FOUND);

        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }

    @Test
    @DisplayName("히스토리 저장 실패 시 예외가 발생한다")
    void updateProjectStatus_whenSaveHistoryFails_shouldThrowException() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(Status.IN_PROGRESS);

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.CONTRACT)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // void 메서드에 예외 설정
        doThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_SAVE_FAILED))
                .when(projectService).updateProjectHistory(
                        anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
                );

        // when & then
        assertThatThrownBy(() -> updateProjectService.updateProjectStatus(
                projectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_SAVE_FAILED);

        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }

    // ========== updateProject 테스트 ==========

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 변경된 필드만 히스토리에 기록된다 - 단일 필드 변경")
    void updateProject_whenSingleFieldChanged_shouldCreateOneHistory() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        Long originalCreator = 50L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Old Title")
                .projectDescription("Old Description")
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        // projectName만 변경
        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "Old Description",
                100L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectService.updateProject(projectId, request, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService, times(1)).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 여러 필드 변경되면 각각 히스토리 생성")
    void updateProject_whenMultipleFieldsChanged_shouldCreateMultipleHistories() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        Long originalCreator = 50L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Old Title")
                .projectDescription("Old Description")
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        // 3개 필드 변경 (title, description, company)
        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "New Description",
                200L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectService.updateProject(projectId, request, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService, times(3)).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 모든 필드 변경되면 5개 히스토리 생성")
    void updateProject_whenAllFieldsChanged_shouldCreateFiveHistories() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        Long originalCreator = 50L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Old Title")
                .projectDescription("Old Description")
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        // 모든 필드 변경
        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "New Description",
                200L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectService.updateProject(projectId, request, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        // 5개 필드: projectTitle, projectDescription, contractStartDate, contractEndDate, clientCompanyId
        verify(projectService, times(5)).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 변경사항이 없으면 히스토리가 생성되지 않는다")
    void updateProject_whenNoFieldsChanged_shouldNotCreateHistory() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        Long originalCreator = 50L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Same Title")
                .projectDescription("Same Description")
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        // 동일한 값으로 요청
        CreateProjectRequest request = new CreateProjectRequest(
                "Same Title",
                "Same Description",
                100L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectService.updateProject(projectId, request, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService, never()).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 올바른 beforeData가 히스토리에 저장된다")
    void updateProject_shouldSaveCorrectBeforeData() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        Long originalCreator = 50L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Original Title")
                .projectDescription("Original Description")
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .clientCompanyId(100L)
                .build();

        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "Original Description",
                100L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectService.updateProject(projectId, request, userIp, userAgent, userId);

        // then
        verify(projectService).saveProjectHistory(argThat(history ->
                history != null &&
                history.getBeforeData().equals("Original Title") &&
                history.getActionType() == ActionType.UPDATE
        ));
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 정보 업데이트 시 예외가 발생한다")
    void updateProject_whenProjectNotFound_shouldThrowException() {
        // given
        Long nonExistentProjectId = 999L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "New Description",
                100L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(nonExistentProjectId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> updateProjectService.updateProject(
                nonExistentProjectId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectService).findProjectById(nonExistentProjectId);
        verify(projectService, never()).getProjectOriginalCreator(anyLong());
        verify(projectService, never()).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 정보 업데이트 시 프로젝트 히스토리를 찾을 수 없는 경우 예외가 발생한다")
    void updateProject_whenProjectHistoryNotFound_shouldThrowException() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project original = Project.builder()
                .projectId(projectId)
                .projectTitle("Old Title")
                .projectDescription("Old Description")
                .build();

        CreateProjectRequest request = new CreateProjectRequest(
                "New Title",
                "Old Description",
                100L,
                List.of(1L),
                List.of(2L),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        given(projectService.findProjectById(projectId)).willReturn(original);
        given(projectService.getProjectOriginalCreator(projectId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> updateProjectService.updateProject(
                projectId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_NOT_FOUND);

        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService, never()).saveProjectHistory(any(ProjectHistory.class));
    }
}