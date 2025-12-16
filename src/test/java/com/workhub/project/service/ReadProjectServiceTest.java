package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.project.dto.response.PagedProjectListResponse;
import com.workhub.project.entity.*;
import com.workhub.projectNode.dto.ProjectNodeCount;
import com.workhub.projectNode.service.ProjectNodeService;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.CompanyStatus;
import com.workhub.userTable.service.CompanyService;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ReadProjectServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private CompanyService companyService;

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
    private Company company;

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
                .clientCompanyId(1L)
                .build();

        project2 = Project.builder()
                .projectId(2L)
                .projectTitle("프로젝트 2")
                .projectDescription("설명 2")
                .status(Status.CONTRACT)
                .contractStartDate(LocalDate.of(2025, 2, 1))
                .contractEndDate(LocalDate.of(2025, 11, 30))
                .clientCompanyId(1L)
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

        company = Company.builder()
                .companyId(1L)
                .companyName("멍뭉이")
                .companyNumber("123-45-67890")
                .tel("010-1234-5678")
                .address("서울시 어딘가")
                .companystatus(CompanyStatus.ACTIVE)
                .build();

        lenient().when(companyService.findById(anyLong())).thenReturn(company);
    }

    @Test
    @DisplayName("CLIENT Role - 자신이 속한 프로젝트만 페이징 조회")
    void givenClientUser_whenProjectListWithPaging_thenReturnOwnProjects() {
        // Given
        setSecurityContext(clientUser);

        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, ProjectNodeCount> workflowCountMap = Map.of(1L, new ProjectNodeCount(3L, 2L));

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).projectId()).isEqualTo(1L);
        assertThat(result.projects().get(0).projectTitle()).isEqualTo("프로젝트 1");
        assertThat(result.hasNext()).isFalse();

        verify(projectService).getClientMemberByUserId(1L);
        verify(projectService).findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("DEVELOPER Role - 자신이 속한 프로젝트만 페이징 조회")
    void givenDeveloperUser_whenProjectListWithPaging_thenReturnOwnProjects() {
        // Given
        setSecurityContext(developerUser);

        List<ProjectDevMember> devMembers = Arrays.asList(devMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, ProjectNodeCount> workflowCountMap = Map.of(1L, new ProjectNodeCount(5L, 4L));

        when(userService.getUserById(2L)).thenReturn(developerUser);
        when(projectService.getDevMemberByUserId(2L)).thenReturn(devMembers);
        when(projectService.findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(devMembers);
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).projectId()).isEqualTo(1L);
        assertThat(result.projects().get(0).totalWorkflow()).isEqualTo(5L);
        assertThat(result.projects().get(0).approveWorkflow()).isEqualTo(4L);

        verify(projectService).getDevMemberByUserId(2L);
    }

    @Test
    @DisplayName("ADMIN Role - 모든 프로젝트 페이징 조회")
    void givenAdminUser_whenProjectListWithPaging_thenReturnAllProjects() {
        // Given
        setSecurityContext(adminUser);

        List<Project> projects = Arrays.asList(project1, project2);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, ProjectNodeCount> workflowCountMap = Map.of(
                1L, new ProjectNodeCount(3L, 2L),
                2L, new ProjectNodeCount(4L, 1L)
        );

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findProjectsWithPaging(isNull(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(Arrays.asList(1L, 2L))).thenReturn(workflowCountMap);

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(2);
        assertThat(result.projects().get(0).projectId()).isEqualTo(1L);
        assertThat(result.projects().get(1).projectId()).isEqualTo(2L);

        verify(projectService).findProjectsWithPaging(isNull(), any(), any(), any(), any(), any(), anyInt());
        verify(projectService, never()).getClientMemberByUserId(anyLong());
        verify(projectService, never()).getDevMemberByUserId(anyLong());
    }

    @Test
    @DisplayName("로그인하지 않은 경우 예외 발생")
    void givenNotLoggedIn_whenProjectListWithPaging_thenThrowException() {
        // Given
        SecurityContextHolder.clearContext();

        // When & Then
        assertThatThrownBy(() -> readProjectService.projectListWithPaging(
                null, null, null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_LOGGED_IN);

        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("사용자가 속한 프로젝트가 없을 경우 빈 페이징 응답 반환")
    void givenNoProjects_whenProjectListWithPaging_thenReturnEmptyResponse() {
        // Given
        setSecurityContext(clientUser);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.size()).isEqualTo(0);

        verify(projectService).getClientMemberByUserId(1L);
        verify(projectService, never()).findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("배치 조회가 올바르게 작동하여 N+1 문제 해결")
    void givenMultipleProjects_whenProjectListWithPaging_thenUseBatchQuery() {
        // Given
        setSecurityContext(adminUser);

        List<Project> projects = Arrays.asList(project1, project2);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findProjectsWithPaging(isNull(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(anyList())).thenReturn(Arrays.asList(clientMember1));
        when(projectService.getDevMemberByProjectIdIn(anyList())).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(Map.of(10L, user1, 20L, user2));
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(anyList())).thenReturn(
                Map.of(
                        1L, new ProjectNodeCount(3L, 2L),
                        2L, new ProjectNodeCount(4L, 1L)
                )
        );

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(2);

        // 프로젝트 개수와 무관하게 각 조회 메서드는 1번씩만 호출되어야 함 (N+1 문제 해결)
        verify(projectService, times(1)).getClientMemberByProjectIdIn(anyList());
        verify(projectService, times(1)).getDevMemberByProjectIdIn(anyList());
        verify(userService, times(1)).getUserMapByUserIdIn(anyList());
        verify(projectNodeService, times(1)).getProjectNodeTotalAndApprovedCountMapByProjectIdIn(anyList());
    }

    @Test
    @DisplayName("페이징 - hasNext가 true일 때 nextCursor 반환")
    void givenMoreProjects_whenProjectListWithPaging_thenReturnNextCursor() {
        // Given
        setSecurityContext(adminUser);

        // size + 1개 조회 (3개)
        Project project3 = Project.builder()
                .projectId(3L)
                .projectTitle("프로젝트 3")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.of(2025, 3, 1))
                .contractEndDate(LocalDate.of(2025, 10, 31))
                .clientCompanyId(100L)
                .build();
        List<Project> projects = Arrays.asList(project1, project2, project3);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findProjectsWithPaging(isNull(), any(), any(), any(), any(), any(), eq(2)))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(anyList())).thenReturn(Collections.emptyList());
        when(projectService.getDevMemberByProjectIdIn(anyList())).thenReturn(Collections.emptyList());
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(Collections.emptyMap());
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(anyList())).thenReturn(Collections.emptyMap());

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 2);

        // Then
        assertThat(result.projects()).hasSize(2); // size만큼만 반환
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(2L); // 마지막 항목의 projectId
    }

    @Test
    @DisplayName("날짜 범위 기본값이 적용됨")
    void givenNoDateRange_whenProjectListWithPaging_thenApplyDefaultDateRange() {
        // Given
        setSecurityContext(adminUser);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findProjectsWithPaging(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        // 날짜 범위가 null이면 1년 전 ~ 현재 날짜로 설정되어 findProjectsWithPaging 호출
        verify(projectService).findProjectsWithPaging(
                isNull(),
                eq(LocalDate.now().minusYears(1)),  // startDate 기본값
                eq(LocalDate.now()),                 // endDate 기본값
                isNull(),                            // status
                any(),                               // sortOrder
                isNull(),                            // cursor
                eq(10)                               // size
        );
    }

    @Test
    @DisplayName("페이지 크기가 최대값(100)을 초과하면 조정됨")
    void givenOversizedPageSize_whenProjectListWithPaging_thenAdjustToMaxSize() {
        // Given
        setSecurityContext(adminUser);

        when(userService.getUserById(3L)).thenReturn(adminUser);
        when(projectService.findProjectsWithPaging(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        readProjectService.projectListWithPaging(
                null, null, null, null, null, 200);  // 최대값 초과

        // Then
        // 페이지 크기가 100으로 조정되어 findProjectsWithPaging 호출
        verify(projectService).findProjectsWithPaging(
                any(), any(), any(), any(), any(), any(), eq(100));
    }

    @Test
    @DisplayName("userMap에 없는 사용자는 필터링되고 경고 로그 출력")
    void givenMissingUserInMap_whenBuildResponse_thenFilterOutAndLogWarning() {
        // Given
        setSecurityContext(clientUser);

        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(20L, user2); // user1 (10L) 누락
        Map<Long, ProjectNodeCount> workflowCountMap = Map.of(1L, new ProjectNodeCount(3L, 1L));

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).clientMembers()).isEmpty(); // user1이 누락되어 필터링됨
        assertThat(result.projects().get(0).devMembers()).hasSize(1); // user2는 존재
        assertThat(result.projects().get(0).totalMembers()).isEqualTo(1); // dev만 카운트
    }

    @Test
    @DisplayName("Company 정보가 포함된 응답 반환")
    void givenProjectList_whenProjectListWithPaging_thenReturnWithCompanyInfo() {
        // Given
        setSecurityContext(clientUser);

        List<ProjectClientMember> clientMembers = Arrays.asList(clientMember1);
        List<Project> projects = Arrays.asList(project1);
        Map<Long, UserTable> userMap = Map.of(10L, user1, 20L, user2);
        Map<Long, ProjectNodeCount> workflowCountMap = Map.of(1L, new ProjectNodeCount(3L, 2L));

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(projectService.getClientMemberByUserId(1L)).thenReturn(clientMembers);
        when(projectService.findProjectsWithPaging(anyList(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(projects);
        when(projectService.getClientMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(clientMembers);
        when(projectService.getDevMemberByProjectIdIn(Arrays.asList(1L))).thenReturn(Arrays.asList(devMember1));
        when(userService.getUserMapByUserIdIn(anyList())).thenReturn(userMap);
        when(projectNodeService.getProjectNodeTotalAndApprovedCountMapByProjectIdIn(Arrays.asList(1L))).thenReturn(workflowCountMap);

        // When
        PagedProjectListResponse result = readProjectService.projectListWithPaging(
                null, null, null, null, null, 10);

        // Then
        assertThat(result.projects()).hasSize(1);
        assertThat(result.projects().get(0).company()).isNotNull();
        assertThat(result.projects().get(0).company().companyId()).isEqualTo(1L);
        assertThat(result.projects().get(0).company().companyName()).isEqualTo("멍뭉이");
    }

    private void setSecurityContext(UserTable user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
