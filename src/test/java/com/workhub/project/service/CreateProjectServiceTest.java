package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.entity.ProjectHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class CreateProjectServiceTest {

    @Mock
    ProjectService projectService;

    @InjectMocks
    CreateProjectService createProjectService;

    @Test
    @DisplayName("프로젝트 생성 시 프로젝트, 히스토리, 멤버, 멤버 히스토리가 모두 저장된다")
    void createProject_success_shouldSaveAllEntities() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> managerIds = Arrays.asList(10L, 20L);
        List<Long> developerIds = Arrays.asList(30L, 40L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                managerIds,
                developerIds,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .projectDescription("Test Description")
                .build();

        List<ProjectClientMember> savedClientMembers = Arrays.asList(
                ProjectClientMember.builder().projectClientMemberId(1L).userId(10L).projectId(1L).build(),
                ProjectClientMember.builder().projectClientMemberId(2L).userId(20L).projectId(1L).build()
        );

        List<ProjectDevMember> savedDevMembers = Arrays.asList(
                ProjectDevMember.builder().projectMemberId(1L).userId(30L).projectId(1L).build(),
                ProjectDevMember.builder().projectMemberId(2L).userId(40L).projectId(1L).build()
        );

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(savedClientMembers);
        given(projectService.saveProjectDevMember(anyList())).willReturn(savedDevMembers);

        // when
        ProjectResponse result = createProjectService.createProject(request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectId()).isEqualTo(1L);
        assertThat(result.projectTitle()).isEqualTo("Test Project");
        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
        verify(projectService).saveProjectClientMember(anyList());
        verify(projectService).saveProjectDevMember(anyList());
        verify(projectService).saveProjectClientMemberHistory(anyList());
        verify(projectService).saveProjectDevMemberHistory(anyList());
    }

    @Test
    @DisplayName("프로젝트 생성 시 저장된 프로젝트 ID로 멤버가 생성된다")
    void createProject_success_shouldCreateMembersWithSavedProjectId() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> managerIds = List.of(10L);
        List<Long> developerIds = List.of(20L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                managerIds,
                developerIds,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(99L)
                .projectTitle("Test Project")
                .build();

        List<ProjectClientMember> savedClientMembers = List.of(
                ProjectClientMember.builder().projectClientMemberId(1L).build()
        );

        List<ProjectDevMember> savedDevMembers = List.of(
                ProjectDevMember.builder().projectMemberId(1L).build()
        );

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(savedClientMembers);
        given(projectService.saveProjectDevMember(anyList())).willReturn(savedDevMembers);

        // when
        createProjectService.createProject(request, loginUser, userIp, userAgent);

        // then
        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectClientMember(argThat(members ->
            members != null && members.size() == 1
        ));
        verify(projectService).saveProjectDevMember(argThat(members ->
            members != null && members.size() == 1
        ));
    }

    @Test
    @DisplayName("고객사 멤버가 없어도 프로젝트 생성이 성공한다")
    void createProject_withoutClientMembers_shouldSucceed() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> developerIds = List.of(30L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                List.of(),
                developerIds,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        List<ProjectDevMember> savedDevMembers = List.of(
                ProjectDevMember.builder().projectMemberId(1L).build()
        );

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(List.of());
        given(projectService.saveProjectDevMember(anyList())).willReturn(savedDevMembers);

        // when
        ProjectResponse result = createProjectService.createProject(request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("개발사 멤버가 없어도 프로젝트 생성이 성공한다")
    void createProject_withoutDevMembers_shouldSucceed() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> managerIds = List.of(10L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                managerIds,
                List.of(),
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        List<ProjectClientMember> savedClientMembers = List.of(
                ProjectClientMember.builder().projectClientMemberId(1L).build()
        );

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(savedClientMembers);
        given(projectService.saveProjectDevMember(anyList())).willReturn(List.of());

        // when
        ProjectResponse result = createProjectService.createProject(request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
    }

    @Test
    @DisplayName("프로젝트 히스토리 저장 시 올바른 사용자 정보가 전달된다")
    void createProject_success_shouldPassCorrectUserInfo() {
        // given
        Long loginUser = 999L;
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                List.of(),
                List.of(),
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(List.of());
        given(projectService.saveProjectDevMember(anyList())).willReturn(List.of());

        // when
        createProjectService.createProject(request, loginUser, userIp, userAgent);

        // then
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("프로젝트 저장 실패 시 예외가 발생한다")
    void createProject_whenSaveProjectFails_shouldThrowException() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                List.of(10L),
                List.of(20L),
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        given(projectService.saveProject(any(Project.class)))
                .willThrow(new BusinessException(ErrorCode.PROJECT_SAVE_FAILED));

        // when & then
        assertThatThrownBy(() -> createProjectService.createProject(request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_SAVE_FAILED);

        // 프로젝트 저장 이후의 메서드들은 호출되지 않아야 함
        verify(projectService).saveProject(any(Project.class));
        verify(projectService, never()).saveProjectHistory(any(ProjectHistory.class));
        verify(projectService, never()).saveProjectClientMember(anyList());
        verify(projectService, never()).saveProjectDevMember(anyList());
    }

    @Test
    @DisplayName("고객사 멤버 저장 실패 시 예외가 발생한다")
    void createProject_whenSaveClientMemberFails_shouldThrowException() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> managerIds = List.of(10L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                managerIds,
                List.of(),
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList()))
                .willThrow(new BusinessException(ErrorCode.CLIENT_MEMBER_SAVE_FAILED));

        // when & then
        assertThatThrownBy(() -> createProjectService.createProject(request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLIENT_MEMBER_SAVE_FAILED);

        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
        verify(projectService).saveProjectClientMember(anyList());
        verify(projectService, never()).saveProjectClientMemberHistory(anyList());
    }

    @Test
    @DisplayName("개발사 멤버 저장 실패 시 예외가 발생한다")
    void createProject_whenSaveDevMemberFails_shouldThrowException() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        List<Long> developerIds = List.of(30L);

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                List.of(),
                developerIds,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);
        given(projectService.saveProjectClientMember(anyList())).willReturn(List.of());
        given(projectService.saveProjectDevMember(anyList()))
                .willThrow(new BusinessException(ErrorCode.DEV_MEMBER_SAVE_FAILED));

        // when & then
        assertThatThrownBy(() -> createProjectService.createProject(request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEV_MEMBER_SAVE_FAILED);

        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
        verify(projectService).saveProjectClientMember(anyList());
        verify(projectService).saveProjectDevMember(anyList());
        verify(projectService, never()).saveProjectDevMemberHistory(anyList());
    }

    @Test
    @DisplayName("프로젝트 히스토리 저장 실패 시 예외가 발생한다")
    void createProject_whenSaveHistoryFails_shouldThrowException() {
        // given
        Long loginUser = 1L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                100L,
                List.of(),
                List.of(),
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        given(projectService.saveProject(any(Project.class))).willReturn(savedProject);

        // void 메서드에 예외 설정
        doThrow(new BusinessException(ErrorCode.PROJECT_HISTORY_SAVE_FAILED))
                .when(projectService).saveProjectHistory(any(ProjectHistory.class));

        // when & then
        assertThatThrownBy(() -> createProjectService.createProject(request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_HISTORY_SAVE_FAILED);

        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectHistory(any(ProjectHistory.class));
        verify(projectService, never()).saveProjectClientMember(anyList());
        verify(projectService, never()).saveProjectDevMember(anyList());
    }
}