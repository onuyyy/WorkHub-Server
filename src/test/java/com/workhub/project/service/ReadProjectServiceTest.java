package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.project.dto.ProjectListResponse;
import com.workhub.project.entity.*;
import com.workhub.projectNode.service.ProjectNodeService;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReadProjectService readProjectService;

    private UserTable clientUser;
    private UserTable developerUser;
    private UserTable adminUser;
    private Project project1;
    private Project project2;
    private ProjectClientMember clientMember1;
    private ProjectDevMember devMember1;
    private UserTable user1;
    private UserTable user2;

    @BeforeEach
    void init() {
        // 사용자 엔티티 생성
        clientUser = UserTable.builder()
                .userId(1L)
                .loginId("client")
                .password("password")
                .role(UserRole.CLIENT)
                .build();

        developerUser = UserTable.builder()
                .userId(2L)
                .loginId("developer")
                .password("password")
                .role(UserRole.DEVELOPER)
                .build();

        adminUser = UserTable.builder()
                .userId(3L)
                .loginId("admin")
                .password("password")
                .role(UserRole.ADMIN)
                .build();

        // 프로젝트 엔티티 생성
        project1 = Project.builder()
                .projectId(1L)
                .projectTitle("프로젝트 1")
                .projectDescription("설명 1")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.of(2025, 1, 1))
                .contractEndDate(LocalDate.of(2025, 12, 31))
                .clientCompanyId(100L)
                .build();

        project2 = Project.builder()
                .projectId(2L)
                .projectTitle("프로젝트 2")
                .projectDescription("설명 2")
                .status(Status.CONTRACT)
                .contractStartDate(LocalDate.of(2025, 2, 1))
                .contractEndDate(LocalDate.of(2025, 11, 30))
                .clientCompanyId(100L)
                .build();

        // 멤버 엔티티 생성
        clientMember1 = ProjectClientMember.builder()
                .projectClientMemberId(1L)
                .userId(10L)
                .projectId(1L)
                .role(Role.READ)
                .assignedAt(LocalDate.now())
                .build();

        devMember1 = ProjectDevMember.builder()
                .projectMemberId(1L)
                .userId(20L)
                .projectId(1L)
                .devPart(DevPart.BE)
                .assignedAt(LocalDate.now())
                .build();

        // 사용자 테이블
        user1 = UserTable.builder()
                .userId(10L)
                .loginId("user1")
                .password("password")
                .role(UserRole.CLIENT)
                .build();

        user2 = UserTable.builder()
                .userId(20L)
                .loginId("user2")
                .password("password")
                .role(UserRole.DEVELOPER)
                .build();
    }

    @Test
    @DisplayName("CLIENT Role - 자신이 속한 프로젝트만 조회")
    void givenClientUser_whenProjectList_thenReturnOwnProjects() {
        // Given
        setSecurityContext(clientUser);
        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, Long> workflowCountMap = Map.of(1L, 3L);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findByProjectIdIn(Arrays.asList(1L))).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).projectId()).isEqualTo(1L);
        assertThat(result.get(0).projectTitle()).isEqualTo("프로젝트 1");
        assertThat(result.get(0).workflowStep()).isEqualTo(3L);
        assertThat(result.get(0).totalMembers()).isEqualTo(2); // client 1명 + dev 1명

        verify(projectService).getClientMemberByUserId(1L);
        verify(projectService).findByProjectIdIn(Arrays.asList(1L));
    }

    @Test
    @DisplayName("DEVELOPER Role - 자신이 속한 프로젝트만 조회")
    void givenDeveloperUser_whenProjectList_thenReturnOwnProjects() {
        // Given
        setSecurityContext(developerUser);
        List<ProjectDevMember> devMembers = Arrays.asList(devMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, Long> workflowCountMap = Map.of(1L, 5L);

        when(userService.getUserById(2L)).thenReturn(developerUser);
        when(projectService.getDevMemberByUserId(2L)).thenReturn(devMembers);
        when(projectService.findByProjectIdIn(Arrays.asList(1L))).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(devMembers);
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).projectId()).isEqualTo(1L);
        assertThat(result.get(0).workflowStep()).isEqualTo(5L);

        verify(projectService).getDevMemberByUserId(2L);
        verify(projectService).findByProjectIdIn(Arrays.asList(1L));
    }

    @Test
    @DisplayName("ADMIN Role - 모든 프로젝트 조회")
    void givenAdminUser_whenProjectList_thenReturnAllProjects() {
        // Given
        setSecurityContext(adminUser);
        List<Project> projects = Arrays.asList(project1, project2);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, Long> workflowCountMap = Map.of(1L, 3L, 2L, 4L);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findAll()).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).projectId()).isEqualTo(1L);
        assertThat(result.get(1).projectId()).isEqualTo(2L);

        verify(projectService).findAll();
        verify(projectService, never()).getClientMemberByUserId(anyLong());
        verify(projectService, never()).getDevMemberByUserId(anyLong());
    }

    @Test
    @DisplayName("로그인하지 않은 경우 예외 발생")
    void givenNotLoggedIn_whenProjectList_thenThrowException() {
        // Given
        SecurityContextHolder.clearContext();

        // When & Then
        assertThatThrownBy(() -> readProjectService.projectList())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_LOGGED_IN);

        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("사용자가 속한 프로젝트가 없을 경우 빈 리스트 반환")
    void givenNoProjects_whenProjectList_thenReturnEmptyList() {
        // Given
        setSecurityContext(clientUser);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).isEmpty();

        verify(projectService).getClientMemberByUserId(1L);
        verify(projectService, never()).findByProjectIdIn(anyList());
        verify(projectService, never()).getClientMemberByProjectIdIn(anyList());
    }

    @Test
    @DisplayName("배치 조회가 올바르게 작동하여 N+1 문제 해결")
    void givenMultipleProjects_whenProjectList_thenUseBatchQuery() {
        // Given
        setSecurityContext(adminUser);
        List<Project> projects = Arrays.asList(project1, project2);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findAll()).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(anyList())).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(anyList())).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(Map.of(10L, user1, 20L, user2));
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(anyList())).thenReturn(Map.of(1L, 3L, 2L, 4L));

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(2);

        // loadBatchData() 메서드가 배치 조회를 올바르게 수행하는지 검증
        // 프로젝트 개수와 무관하게 각 조회 메서드는 1번씩만 호출되어야 함 (N+1 문제 해결)
        verify(projectService, times(1)).getClientMemberByProjectIdIn(anyList());
        verify(projectService, times(1)).getDevMemberByProjectIdIn(anyList());
        verify(userService, times(1)).getUserMapByUserIdIn(anyList());
        verify(projectNodeService, times(1)).getProjectNodeCountMapByProjectIdIn(anyList());
    }

    @Test
    @DisplayName("userMap에 없는 사용자는 필터링되고 경고 로그 출력")
    void givenMissingUserInMap_whenBuildResponse_thenFilterOutAndLogWarning() {
        // Given
        setSecurityContext(clientUser);
        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(20L, user2); // user1 (10L) 누락
        Map<Long, Long> workflowCountMap = Map.of(1L, 3L);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findByProjectIdIn(Arrays.asList(1L))).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientMembers()).isEmpty(); // user1이 누락되어 필터링됨
        assertThat(result.get(0).devMembers()).hasSize(1); // user2는 존재
        assertThat(result.get(0).totalMembers()).isEqualTo(1); // dev만 카운트
    }

    @Test
    @DisplayName("멤버가 없는 프로젝트도 정상 처리")
    void givenProjectWithNoMembers_whenProjectList_thenReturnWithEmptyMembers() {
        // Given
        setSecurityContext(adminUser);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Collections.emptyMap();
        Map<Long, Long> workflowCountMap = Map.of(1L, 3L);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findAll()).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Collections.emptyList());
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Collections.emptyList());
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientMembers()).isEmpty();
        assertThat(result.get(0).devMembers()).isEmpty();
        assertThat(result.get(0).totalMembers()).isEqualTo(0);
    }

    @Test
    @DisplayName("워크플로우 개수가 0인 프로젝트도 정상 처리")
    void givenProjectWithNoWorkflow_whenProjectList_thenReturnWithZeroWorkflow() {
        // Given
        setSecurityContext(clientUser);
        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, Long> workflowCountMap = Collections.emptyMap(); // 워크플로우 없음

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findByProjectIdIn(Arrays.asList(1L))).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).workflowStep()).isEqualTo(0L); // 기본값 0
    }

    @Test
    @DisplayName("Company 정보가 포함된 응답 반환")
    void givenProjectList_whenProjectList_thenReturnWithCompanyInfo() {
        // Given
        setSecurityContext(clientUser);
        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, Long> workflowCountMap = Map.of(1L, 3L);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findByProjectIdIn(Arrays.asList(1L))).thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        List<ProjectListResponse> result = readProjectService.projectList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).company()).isNotNull();
        assertThat(result.get(0).company().companyId()).isEqualTo(1L);
        assertThat(result.get(0).company().companyName()).isEqualTo("멍뭉이");
    }

    private void setSecurityContext(UserTable user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
