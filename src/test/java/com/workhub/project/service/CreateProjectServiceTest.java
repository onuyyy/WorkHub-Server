package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.entity.DevPart;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.entity.Role;
import com.workhub.project.entity.Status;
import org.junit.jupiter.api.BeforeEach;
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

    private CreateProjectRequest request;
    private Project mockProject;
    private List<ProjectClientMember> mockClientMembers;
    private List<ProjectDevMember> mockDevMembers;

    @BeforeEach
    void init() {
        request = new CreateProjectRequest(
                "신규 프로젝트",
                "프로젝트 설명",
                100L,
                Arrays.asList(1L, 2L),  // managerIds
                Arrays.asList(10L, 20L),  // developerIds
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("신규 프로젝트")
                .projectDescription("프로젝트 설명")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusMonths(6))
                .clientCompanyId(100L)
                .build();

        mockClientMembers = Arrays.asList(
                ProjectClientMember.builder()
                        .projectClientMemberId(1L)
                        .userId(1L)
                        .projectId(1L)
                        .role(Role.READ)
                        .assignedAt(LocalDate.now())
                        .build(),
                ProjectClientMember.builder()
                        .projectClientMemberId(2L)
                        .userId(2L)
                        .projectId(1L)
                        .role(Role.READ)
                        .assignedAt(LocalDate.now())
                        .build()
        );

        mockDevMembers = Arrays.asList(
                ProjectDevMember.builder()
                        .projectMemberId(10L)
                        .userId(10L)
                        .projectId(1L)
                        .devPart(DevPart.BE)
                        .assignedAt(LocalDate.now())
                        .build(),
                ProjectDevMember.builder()
                        .projectMemberId(20L)
                        .userId(20L)
                        .projectId(1L)
                        .devPart(DevPart.BE)
                        .assignedAt(LocalDate.now())
                        .build()
        );
    }

    @Test
    @DisplayName("프로젝트를 생성하면 프로젝트와 멤버가 저장되고 히스토리가 기록된다")
    void givenCreateProjectRequest_whenCreateProject_thenSaveProjectAndMembersAndRecordHistory() {
        // given
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(mockClientMembers);
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(mockDevMembers);

        // when
        ProjectResponse response = createProjectService.createProject(request);

        // then
        // 1. 프로젝트가 저장되었는지 확인
        verify(projectService).saveProject(any(Project.class));

        // 2. 프로젝트 히스토리가 기록되었는지 확인
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(1L),
                eq(ActionType.CREATE),
                any(ProjectHistorySnapshot.class)
        );

        // 3. 클라이언트 멤버가 저장되었는지 확인
        verify(projectService).saveClientMembers(eq(Arrays.asList(1L, 2L)), eq(1L));

        // 4. 클라이언트 멤버 히스토리가 기록되었는지 확인 (2개)
        verify(historyRecorder, times(2)).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                anyLong(),
                eq(ActionType.CREATE),
                any(ProjectClientMember.class)
        );

        // 5. 개발자 멤버가 저장되었는지 확인
        verify(projectService).saveDevMembers(eq(Arrays.asList(10L, 20L)), eq(1L));

        // 6. 개발자 멤버 히스토리가 기록되었는지 확인 (2개)
        verify(historyRecorder, times(2)).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                anyLong(),
                eq(ActionType.CREATE),
                any(ProjectDevMember.class)
        );

        // 7. 응답 검증
        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.projectTitle()).isEqualTo("신규 프로젝트");
    }

    @Test
    @DisplayName("프로젝트 생성 시 실행 순서가 올바르게 동작한다")
    void givenCreateProjectRequest_whenCreateProject_thenExecuteInCorrectOrder() {
        // given
        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(mockClientMembers);
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(mockDevMembers);

        // when
        createProjectService.createProject(request);

        // then - 순서 검증 (InOrder 사용)
        var inOrder = inOrder(projectService, historyRecorder);

        // 1. 프로젝트 저장
        inOrder.verify(projectService).saveProject(any(Project.class));

        // 2. 프로젝트 히스토리 기록
        inOrder.verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(1L),
                eq(ActionType.CREATE),
                any(ProjectHistorySnapshot.class)
        );

        // 3. 클라이언트 멤버 저장
        inOrder.verify(projectService).saveClientMembers(anyList(), anyLong());

        // 4. 개발자 멤버 저장
        inOrder.verify(projectService).saveDevMembers(anyList(), anyLong());
    }

    @Test
    @DisplayName("빈 멤버 리스트로 프로젝트를 생성해도 정상 동작한다")
    void givenEmptyMemberList_whenCreateProject_thenSaveProjectWithoutMembers() {
        // given
        CreateProjectRequest emptyMemberRequest = new CreateProjectRequest(
                "신규 프로젝트",
                "프로젝트 설명",
                100L,
                List.of(),  // 빈 클라이언트 멤버
                List.of(),  // 빈 개발자 멤버
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        when(projectService.saveProject(any(Project.class))).thenReturn(mockProject);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of());
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of());

        // when
        ProjectResponse response = createProjectService.createProject(emptyMemberRequest);

        // then
        // 1. 프로젝트는 저장됨
        verify(projectService).saveProject(any(Project.class));

        // 2. 멤버 저장은 호출되지만 빈 리스트
        verify(projectService).saveClientMembers(eq(List.of()), eq(1L));
        verify(projectService).saveDevMembers(eq(List.of()), eq(1L));

        // 3. 멤버 히스토리는 기록되지 않음 (빈 리스트이므로)
        verify(historyRecorder, never()).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                anyLong(),
                eq(ActionType.CREATE),
                any()
        );
        verify(historyRecorder, never()).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                anyLong(),
                eq(ActionType.CREATE),
                any()
        );

        // 4. 응답은 정상적으로 반환됨
        assertThat(response).isNotNull();
    }
}
