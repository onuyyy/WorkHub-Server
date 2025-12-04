package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.entity.*;
import com.workhub.userTable.entity.UserTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.workhub.userTable.entity.UserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private CreateProjectService createProjectService;

    private CreateProjectRequest mockRequest;
    private Project mockProject;
    private ProjectClientMember mockClientMember;
    private ProjectDevMember mockDevMember;

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

        mockRequest = new CreateProjectRequest(
                "테스트 프로젝트",
                "프로젝트 설명입니다",
                100L,
                Arrays.asList(1L, 2L),
                Arrays.asList(3L, 4L),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("테스트 프로젝트")
                .projectDescription("프로젝트 설명입니다")
                .status(Status.CONTRACT)
                .contractStartDate(LocalDate.of(2025, 1, 1))
                .contractEndDate(LocalDate.of(2025, 6, 30))
                .clientCompanyId(100L)
                .build();

        mockClientMember = ProjectClientMember.builder()
                .projectClientMemberId(1L)
                .userId(1L)
                .projectId(1L)
                .role(Role.READ)
                .assignedAt(LocalDate.now())
                .build();

        mockDevMember = ProjectDevMember.builder()
                .projectMemberId(1L)
                .userId(3L)
                .projectId(1L)
                .devPart(DevPart.BE)
                .assignedAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("프로젝트 생성 요청 시 프로젝트가 생성되고 응답이 반환된다.")
    void givenCreateProjectRequest_whenCreateProject_thenReturnProjectResponse() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(mockDevMember));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        ProjectResponse result = createProjectService.createProject(mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.projectId()).isEqualTo(1L);
        assertThat(result.projectTitle()).isEqualTo("테스트 프로젝트");
        assertThat(result.projectDescription()).isEqualTo("프로젝트 설명입니다");
        assertThat(result.status()).isEqualTo(Status.CONTRACT);
        assertThat(result.contractStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.contractEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));

        verify(projectService).saveProject(any(Project.class));
        verify(historyRecorder).recordHistory(HistoryType.PROJECT, 1L, ActionType.CREATE, "프로젝트 설명입니다");
    }

    @Test
    @DisplayName("프로젝트 생성 시 클라이언트 멤버가 저장되고 히스토리가 기록된다.")
    void givenCreateProjectRequest_whenCreateProject_thenSaveClientMembersAndHistory() {
        ProjectClientMember clientMember1 = ProjectClientMember.builder()
                .projectClientMemberId(1L)
                .userId(1L)
                .projectId(1L)
                .build();

        ProjectClientMember clientMember2 = ProjectClientMember.builder()
                .projectClientMemberId(2L)
                .userId(2L)
                .projectId(1L)
                .build();

        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(clientMember1, clientMember2));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(mockDevMember));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        createProjectService.createProject(mockRequest);

        verify(projectService).saveProjectClientMember(argThat(list ->
                list.size() == 2 &&
                list.stream().anyMatch(m -> m.getUserId().equals(1L)) &&
                list.stream().anyMatch(m -> m.getUserId().equals(2L))
        ));
        verify(projectService).saveProjectClientMemberHistory(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("프로젝트 생성 시 개발 멤버가 저장되고 히스토리가 기록된다.")
    void givenCreateProjectRequest_whenCreateProject_thenSaveDevMembersAndHistory() {
        ProjectDevMember devMember1 = ProjectDevMember.builder()
                .projectMemberId(1L)
                .userId(3L)
                .projectId(1L)
                .build();

        ProjectDevMember devMember2 = ProjectDevMember.builder()
                .projectMemberId(2L)
                .userId(4L)
                .projectId(1L)
                .build();

        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(devMember1, devMember2));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        createProjectService.createProject(mockRequest);

        verify(projectService).saveProjectDevMember(argThat(list ->
                list.size() == 2 &&
                list.stream().anyMatch(m -> m.getUserId().equals(3L)) &&
                list.stream().anyMatch(m -> m.getUserId().equals(4L))
        ));
        verify(projectService).saveProjectDevMemberHistory(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("프로젝트 생성 시 요청 데이터가 엔티티로 올바르게 매핑된다.")
    void givenCreateProjectRequest_whenCreateProject_thenRequestMappedToEntity() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(mockDevMember));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        createProjectService.createProject(mockRequest);

        verify(projectService).saveProject(argThat(project ->
                project.getProjectTitle().equals("테스트 프로젝트") &&
                project.getProjectDescription().equals("프로젝트 설명입니다") &&
                project.getStatus().equals(Status.CONTRACT) &&
                project.getClientCompanyId().equals(100L)
        ));
    }

    @Test
    @DisplayName("프로젝트 저장 실패 시 예외가 발생한다.")
    void givenCreateProjectRequest_whenSaveProjectFails_thenThrowException() {
        when(projectService.saveProject(any(Project.class)))
                .thenThrow(new RuntimeException("프로젝트 저장 실패"));

        assertThatThrownBy(() -> createProjectService.createProject(mockRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("프로젝트 저장 실패");

        verify(projectService).saveProject(any(Project.class));
        verify(historyRecorder, never()).recordHistory(any(), anyLong(), any(), anyString());
        verify(projectService, never()).saveProjectClientMember(anyList());
        verify(projectService, never()).saveProjectDevMember(anyList());
    }

    @Test
    @DisplayName("히스토리 기록 실패 시 예외가 전파된다.")
    void givenCreateProjectRequest_whenHistoryRecordFails_thenThrowException() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        doThrow(new RuntimeException("히스토리 저장 실패"))
                .when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        assertThatThrownBy(() -> createProjectService.createProject(mockRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("히스토리 저장 실패");

        verify(projectService).saveProject(any(Project.class));
        verify(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        verify(projectService, never()).saveProjectClientMember(anyList());
    }

    @Test
    @DisplayName("클라이언트 멤버 저장 실패 시 예외가 전파된다.")
    void givenCreateProjectRequest_whenSaveClientMemberFails_thenThrowException() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        when(projectService.saveProjectClientMember(anyList()))
                .thenThrow(new RuntimeException("클라이언트 멤버 저장 실패"));

        assertThatThrownBy(() -> createProjectService.createProject(mockRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("클라이언트 멤버 저장 실패");

        verify(projectService).saveProject(any(Project.class));
        verify(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        verify(projectService).saveProjectClientMember(anyList());
        verify(projectService, never()).saveProjectClientMemberHistory(anyList());
    }

    @Test
    @DisplayName("개발 멤버 저장 실패 시 예외가 전파된다.")
    void givenCreateProjectRequest_whenSaveDevMemberFails_thenThrowException() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        when(projectService.saveProjectDevMember(anyList()))
                .thenThrow(new RuntimeException("개발 멤버 저장 실패"));

        assertThatThrownBy(() -> createProjectService.createProject(mockRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("개발 멤버 저장 실패");

        verify(projectService).saveProject(any(Project.class));
        verify(projectService).saveProjectClientMember(anyList());
        verify(projectService).saveProjectDevMember(anyList());
        verify(projectService, never()).saveProjectDevMemberHistory(anyList());
    }

    @Test
    @DisplayName("빈 클라이언트 멤버 리스트로 프로젝트 생성 시 빈 리스트가 저장된다.")
    void givenEmptyClientMemberList_whenCreateProject_thenSaveEmptyList() {
        CreateProjectRequest requestWithEmptyClients = new CreateProjectRequest(
                "테스트 프로젝트",
                "프로젝트 설명입니다",
                100L,
                Collections.emptyList(),
                Arrays.asList(3L, 4L),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Collections.emptyList());
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(mockDevMember));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        ProjectResponse result = createProjectService.createProject(requestWithEmptyClients);

        assertThat(result).isNotNull();
        verify(projectService).saveProjectClientMember(argThat(List::isEmpty));
        verify(projectService).saveProjectClientMemberHistory(argThat(List::isEmpty));
    }

    @Test
    @DisplayName("빈 개발 멤버 리스트로 프로젝트 생성 시 빈 리스트가 저장된다.")
    void givenEmptyDevMemberList_whenCreateProject_thenSaveEmptyList() {
        CreateProjectRequest requestWithEmptyDevs = new CreateProjectRequest(
                "테스트 프로젝트",
                "프로젝트 설명입니다",
                100L,
                Arrays.asList(1L, 2L),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Collections.emptyList());
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        ProjectResponse result = createProjectService.createProject(requestWithEmptyDevs);

        assertThat(result).isNotNull();
        verify(projectService).saveProjectDevMember(argThat(List::isEmpty));
        verify(projectService).saveProjectDevMemberHistory(argThat(List::isEmpty));
    }

    @Test
    @DisplayName("프로젝트 생성 시 모든 저장 메서드가 순차적으로 호출된다.")
    void givenCreateProjectRequest_whenCreateProject_thenAllSaveMethodsCalledInOrder() {
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveProjectClientMember(anyList())).thenReturn(Arrays.asList(mockClientMember));
        when(projectService.saveProjectDevMember(anyList())).thenReturn(Arrays.asList(mockDevMember));
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        doNothing().when(projectService).saveProjectClientMemberHistory(anyList());
        doNothing().when(projectService).saveProjectDevMemberHistory(anyList());

        createProjectService.createProject(mockRequest);

        var inOrder = inOrder(projectService, historyRecorder);
        inOrder.verify(projectService).saveProject(any(Project.class));
        inOrder.verify(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
        inOrder.verify(projectService).saveProjectClientMember(anyList());
        inOrder.verify(projectService).saveProjectClientMemberHistory(anyList());
        inOrder.verify(projectService).saveProjectDevMember(anyList());
        inOrder.verify(projectService).saveProjectDevMemberHistory(anyList());
    }
}