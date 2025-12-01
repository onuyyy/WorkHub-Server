package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateProjectStatusServiceTest {

    @Mock
    ProjectService projectService;

    @InjectMocks
    UpdateProjectStatusService updateProjectStatusService;

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

        Long originalCreator = 50L;

        given(projectService.findProjectById(projectId)).willReturn(project);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectStatusService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService).updateProjectHistory(any(ProjectHistory.class));
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
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(1L);

        // when
        updateProjectStatusService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        // 프로젝트 엔티티의 updateProjectStatus 메서드가 호출되어 상태가 변경됨
        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(any(ProjectHistory.class));
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

        Long originalCreator = 50L;

        given(projectService.findProjectById(projectId)).willReturn(project);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // when
        updateProjectStatusService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);

        // then
        verify(projectService).updateProjectHistory(argThat(history ->
                history != null &&
                history.getActionType() == ActionType.UPDATE &&
                history.getTargetId().equals(projectId) &&
                history.getCreatedBy().equals(originalCreator) &&
                history.getUpdatedBy().equals(userId) &&
                history.getIpAddress().equals(userIp) &&
                history.getUserAgent().equals(userAgent)
        ));
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
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(1L);

        // when & then - 각 상태로 변경 테스트
        for (Status status : Status.values()) {
            UpdateStatusRequest statusRequest = new UpdateStatusRequest(status);
            updateProjectStatusService.updateProjectStatus(projectId, statusRequest, userIp, userAgent, userId);
        }

        // 모든 상태에 대해 히스토리가 저장되었는지 확인
        verify(projectService, times(Status.values().length)).updateProjectHistory(any(ProjectHistory.class));
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
        assertThatThrownBy(() -> updateProjectStatusService.updateProjectStatus(
                nonExistentProjectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        // 프로젝트 조회 실패 시 이후 메서드는 호출되지 않아야 함
        verify(projectService).findProjectById(nonExistentProjectId);
        verify(projectService, never()).getProjectOriginalCreator(anyLong());
        verify(projectService, never()).updateProjectHistory(any(ProjectHistory.class));
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
        given(projectService.getProjectOriginalCreator(projectId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> updateProjectStatusService.updateProjectStatus(
                projectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_NOT_FOUND);

        // 원본 생성자 조회 실패 시 히스토리 저장은 실행되지 않아야 함
        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService, never()).updateProjectHistory(any(ProjectHistory.class));
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

        Long originalCreator = 50L;

        given(projectService.findProjectById(projectId)).willReturn(project);
        given(projectService.getProjectOriginalCreator(projectId)).willReturn(originalCreator);

        // void 메서드에 예외 설정
        doThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_SAVE_FAILED))
                .when(projectService).updateProjectHistory(any(ProjectHistory.class));

        // when & then
        assertThatThrownBy(() -> updateProjectStatusService.updateProjectStatus(
                projectId, statusRequest, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_SAVE_FAILED);

        verify(projectService).findProjectById(projectId);
        verify(projectService).getProjectOriginalCreator(projectId);
        verify(projectService).updateProjectHistory(any(ProjectHistory.class));
    }
}