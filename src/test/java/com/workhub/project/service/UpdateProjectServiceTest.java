package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.request.UpdateStatusRequest;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.entity.DevPart;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.entity.Role;
import com.workhub.project.entity.Status;
import com.workhub.project.event.ProjectStatusChangedEvent;
import com.workhub.project.event.ProjectUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private HistoryRecorder historyRecorder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateProjectService updateProjectService;

    private CreateProjectRequest updateRequest;
    private Project mockProject;
    private List<ProjectClientMember> existingClientMembers;
    private List<ProjectDevMember> existingDevMembers;

    @BeforeEach
    void init() {
        updateRequest = new CreateProjectRequest(
                "업데이트된 프로젝트",
                "업데이트된 설명",
                100L,
                Arrays.asList(1L, 3L),  // managerIds: 1L 유지, 2L 삭제, 3L 추가
                Arrays.asList(10L, 30L),  // developerIds: 10L 유지, 20L 삭제, 30L 추가
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("기존 프로젝트")
                .projectDescription("기존 설명")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusMonths(6))
                .clientCompanyId(100L)
                .build();

        // 기존 멤버: 1L, 2L
        existingClientMembers = Arrays.asList(
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

        // 기존 멤버: 10L, 20L
        existingDevMembers = Arrays.asList(
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
    @DisplayName("프로젝트를 업데이트하면 프로젝트 정보가 변경되고 히스토리가 기록된다")
    void givenUpdateRequest_whenUpdateProject_thenUpdateProjectAndRecordHistory() {
        // given
        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of(
                ProjectClientMember.builder()
                        .projectClientMemberId(3L)
                        .userId(3L)
                        .projectId(1L)
                        .role(Role.READ)
                        .assignedAt(LocalDate.now())
                        .build()
        ));
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of(
                ProjectDevMember.builder()
                        .projectMemberId(30L)
                        .userId(30L)
                        .projectId(1L)
                        .devPart(DevPart.BE)
                        .assignedAt(LocalDate.now())
                        .build()
        ));

        // when
        ProjectResponse response = updateProjectService.updateProject(1L, updateRequest);

        // then
        // 1. 프로젝트가 조회되었는지 확인
        verify(projectService).findProjectById(1L);

        // 2. 프로젝트 업데이트 히스토리가 기록되었는지 확인
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(1L),
                eq(ActionType.UPDATE),
                any(ProjectHistorySnapshot.class)
        );

        // 3. 기존 멤버가 조회되었는지 확인
        verify(projectService).getClientMemberByProjectId(1L);
        verify(projectService).getDevMemberByProjectId(1L);

        // 4. 응답 검증
        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        verify(eventPublisher).publishEvent(any(ProjectUpdatedEvent.class));
    }

    @Test
    @DisplayName("멤버 추가 시 새로운 멤버가 저장되고 히스토리가 기록된다")
    void givenNewMembers_whenUpdateProject_thenSaveNewMembersAndRecordHistory() {
        // given
        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);

        ProjectClientMember newClientMember = ProjectClientMember.builder()
                .projectClientMemberId(3L)
                .userId(3L)
                .projectId(1L)
                .role(Role.READ)
                .assignedAt(LocalDate.now())
                .build();

        ProjectDevMember newDevMember = ProjectDevMember.builder()
                .projectMemberId(30L)
                .userId(30L)
                .projectId(1L)
                .devPart(DevPart.BE)
                .assignedAt(LocalDate.now())
                .build();

        when(projectService.saveClientMembers(eq(List.of(3L)), eq(1L)))
                .thenReturn(List.of(newClientMember));
        when(projectService.saveDevMembers(eq(List.of(30L)), eq(1L)))
                .thenReturn(List.of(newDevMember));

        // when
        updateProjectService.updateProject(1L, updateRequest);

        // then
        // 추가할 멤버: 클라이언트 3L, 개발자 30L
        verify(projectService).saveClientMembers(eq(List.of(3L)), eq(1L));
        verify(projectService).saveDevMembers(eq(List.of(30L)), eq(1L));

        // 추가 히스토리 기록 확인
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                eq(3L),
                eq(ActionType.CREATE),
                eq(newClientMember)
        );
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                eq(30L),
                eq(ActionType.CREATE),
                eq(newDevMember)
        );
        verify(eventPublisher).publishEvent(any(ProjectUpdatedEvent.class));
    }

    @Test
    @DisplayName("멤버 삭제 시 removeMember가 호출되고 삭제 히스토리가 기록된다")
    void givenMembersToRemove_whenUpdateProject_thenRemoveMembersAndRecordHistory() {
        // given
        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of());
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of());

        // when
        updateProjectService.updateProject(1L, updateRequest);

        // then
        // 삭제할 멤버: 클라이언트 2L, 개발자 20L
        // 각 멤버의 removeMember()가 호출되었는지 확인할 수 없지만, 삭제 히스토리가 기록되었는지 확인
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                eq(1L),
                eq(ActionType.DELETE),
                any(ProjectClientMember.class)
        );
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                eq(1L),
                eq(ActionType.DELETE),
                any(ProjectDevMember.class)
        );
        verify(eventPublisher).publishEvent(any(ProjectUpdatedEvent.class));
    }

    @Test
    @DisplayName("프로젝트 업데이트 시 실행 순서가 올바르게 동작한다")
    void givenUpdateRequest_whenUpdateProject_thenExecuteInCorrectOrder() {
        // given
        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of());
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of());

        // when
        updateProjectService.updateProject(1L, updateRequest);

        // then - 순서 검증
        var inOrder = inOrder(projectService, historyRecorder);

        // 1. 프로젝트 조회 및 업데이트
        inOrder.verify(projectService).findProjectById(1L);
        inOrder.verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT),
                eq(1L),
                eq(ActionType.UPDATE),
                any(ProjectHistorySnapshot.class)
        );

        // 2. 기존 멤버 조회
        inOrder.verify(projectService).getClientMemberByProjectId(1L);
        inOrder.verify(projectService).getDevMemberByProjectId(1L);

        // 3. 멤버 저장 (추가)
        inOrder.verify(projectService).saveClientMembers(anyList(), eq(1L));
        inOrder.verify(projectService).saveDevMembers(anyList(), eq(1L));
    }

    @Test
    @DisplayName("멤버 변경사항이 없으면 추가/삭제가 발생하지 않는다")
    void givenNoMemberChanges_whenUpdateProject_thenNoMemberOperations() {
        // given - 요청 멤버가 기존과 동일
        CreateProjectRequest sameRequest = new CreateProjectRequest(
                "업데이트된 프로젝트",
                "업데이트된 설명",
                100L,
                Arrays.asList(1L, 2L),  // 기존과 동일
                Arrays.asList(10L, 20L),  // 기존과 동일
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of());
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of());

        // when
        updateProjectService.updateProject(1L, sameRequest);

        // then
        // 추가할 멤버가 없으므로 빈 리스트로 호출됨
        verify(projectService).saveClientMembers(eq(List.of()), eq(1L));
        verify(projectService).saveDevMembers(eq(List.of()), eq(1L));

        // 삭제 히스토리는 기록되지 않음
        verify(historyRecorder, never()).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                anyLong(),
                eq(ActionType.DELETE),
                any()
        );
        verify(historyRecorder, never()).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                anyLong(),
                eq(ActionType.DELETE),
                any()
        );
        verify(eventPublisher).publishEvent(any(ProjectUpdatedEvent.class));
    }

    @Test
    @DisplayName("모든 멤버를 삭제하고 새로운 멤버를 추가할 수 있다")
    void givenCompletelyNewMembers_whenUpdateProject_thenRemoveAllAndAddNew() {
        // given - 완전히 새로운 멤버로 교체
        CreateProjectRequest newMembersRequest = new CreateProjectRequest(
                "업데이트된 프로젝트",
                "업데이트된 설명",
                100L,
                Arrays.asList(100L, 200L),  // 완전히 새로운 멤버
                Arrays.asList(1000L, 2000L),  // 완전히 새로운 멤버
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        when(projectService.getClientMemberByProjectId(1L)).thenReturn(existingClientMembers);
        when(projectService.getDevMemberByProjectId(1L)).thenReturn(existingDevMembers);
        when(projectService.saveClientMembers(anyList(), anyLong())).thenReturn(List.of());
        when(projectService.saveDevMembers(anyList(), anyLong())).thenReturn(List.of());

        // when
        updateProjectService.updateProject(1L, newMembersRequest);

        // then
        // 모든 기존 멤버 삭제 (2개씩)
        verify(historyRecorder, times(2)).recordHistory(
                eq(HistoryType.PROJECT_CLIENT_MEMBER),
                eq(1L),
                eq(ActionType.DELETE),
                any(ProjectClientMember.class)
        );
        verify(historyRecorder, times(2)).recordHistory(
                eq(HistoryType.PROJECT_DEV_MEMBER),
                eq(1L),
                eq(ActionType.DELETE),
                any(ProjectDevMember.class)
        );

        // 새로운 멤버 추가
        verify(projectService).saveClientMembers(eq(Arrays.asList(100L, 200L)), eq(1L));
        verify(projectService).saveDevMembers(eq(Arrays.asList(1000L, 2000L)), eq(1L));
    }

    @Test
    @DisplayName("프로젝트 상태 변경 시 히스토리와 알림이 기록된다")
    void givenStatusChange_whenUpdateStatus_thenHistoryAndNotification() {
        mockProject.updateProjectStatus(Status.IN_PROGRESS);
        when(projectService.findProjectById(1L)).thenReturn(mockProject);
        UpdateStatusRequest request = new UpdateStatusRequest(Status.COMPLETED);

        updateProjectService.updateProjectStatus(1L, request);

        verify(historyRecorder).recordHistory(eq(HistoryType.PROJECT), eq(1L), eq(ActionType.UPDATE), any(ProjectHistorySnapshot.class));
        verify(eventPublisher).publishEvent(any(ProjectStatusChangedEvent.class));
        assertThat(mockProject.getStatus()).isEqualTo(Status.COMPLETED);
    }
}
