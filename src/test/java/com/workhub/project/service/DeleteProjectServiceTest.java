package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteProjectServiceTest {

    @Mock
    ProjectService projectService;

    @InjectMocks
    DeleteProjectService deleteProjectService;

    @Test
    @DisplayName("프로젝트 삭제 시 프로젝트 조회, soft delete, 히스토리 저장이 모두 실행된다")
    void deleteProject_success_shouldMarkDeletedAndSaveHistory() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.IN_PROGRESS)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        deleteProjectService.deleteProject(projectId, userId, userIp, userAgent);

        // then
        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.DELETE),
                anyString(),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("프로젝트 삭제 시 올바른 beforeData가 히스토리에 저장된다")
    void deleteProject_shouldSaveCorrectBeforeData() {
        // given
        Long projectId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.DELIVERY)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        deleteProjectService.deleteProject(projectId, userId, userIp, userAgent);

        // then
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.DELETE),
                eq("DELIVERY"),  // beforeStatus
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("프로젝트 삭제 시 올바른 사용자 정보가 전달된다")
    void deleteProject_success_shouldPassCorrectUserInfo() {
        // given
        Long projectId = 1L;
        Long userId = 999L;
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";

        Project project = Project.builder()
                .projectId(projectId)
                .projectTitle("Test Project")
                .status(Status.CONTRACT)
                .build();

        given(projectService.findProjectById(projectId)).willReturn(project);

        // when
        deleteProjectService.deleteProject(projectId, userId, userIp, userAgent);

        // then
        verify(projectService).updateProjectHistory(
                eq(projectId),
                eq(ActionType.DELETE),
                anyString(),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 삭제 시 예외가 발생한다")
    void deleteProject_whenProjectNotFound_shouldThrowException() {
        // given
        Long nonExistentProjectId = 999L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        given(projectService.findProjectById(nonExistentProjectId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> deleteProjectService.deleteProject(
                nonExistentProjectId, userId, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        // 프로젝트 조회 실패 시 히스토리는 저장되지 않아야 함
        verify(projectService).findProjectById(nonExistentProjectId);
        verify(projectService, never()).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }

    @Test
    @DisplayName("히스토리 저장 실패 시 예외가 발생한다")
    void deleteProject_whenSaveHistoryFails_shouldThrowException() {
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

        // void 메서드에 예외 설정
        doThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_SAVE_FAILED))
                .when(projectService).updateProjectHistory(
                        anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
                );

        // when & then
        assertThatThrownBy(() -> deleteProjectService.deleteProject(
                projectId, userId, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_SAVE_FAILED);

        verify(projectService).findProjectById(projectId);
        verify(projectService).updateProjectHistory(
                anyLong(), any(ActionType.class), anyString(), anyString(), anyString(), anyLong()
        );
    }
}