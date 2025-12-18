package com.workhub.projectNode.service;

import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.event.ProjectNodeCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class CreateProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @Mock
    ProjectNodeValidator projectNodeValidator;

    @Mock
    HistoryRecorder historyRecorder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CreateProjectNodeService createProjectNodeService;

    private MockedStatic<SecurityUtil> securityUtil;
    private CreateNodeRequest request;
    private Project testProject;
    private ProjectNode testNode;

    @BeforeEach
    void setUp() {
        // SecurityUtil static method mock
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(1L);
        securityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

        // Test data
        request = new CreateNodeRequest(
                "테스트 노드",
                "테스트 설명",
                10L,
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        testProject = Project.builder()
                .projectId(100L)
                .projectTitle("테스트 프로젝트")
                .status(Status.CONTRACT)
                .build();

        testNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(100L)
                .title("테스트 노드")
                .description("테스트 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .developerUserId(10L)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusDays(30))
                .build();
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @Test
    @DisplayName("노드 생성 성공 - 첫 번째 노드")
    void createNode_Success_FirstNode() {
        // given
        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeValidator.validateProjectAndDevMember(anyLong(), anyLong()))
                .willReturn(testProject);
        given(projectNodeService.findMaxNodeOrderByProjectId(anyLong())).willReturn(0);
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(testNode);

        // when
        CreateNodeResponse response = createProjectNodeService.createNode(100L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.projectNodeId()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("테스트 노드");
        assertThat(testProject.getStatus()).isEqualTo(Status.IN_PROGRESS);

        verify(projectNodeValidator).validateLoginUserPermission(100L, 1L);
        verify(projectNodeValidator).validateProjectAndDevMember(100L, 10L);
        verify(projectNodeService).findMaxNodeOrderByProjectId(100L);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(eventPublisher).publishEvent(any(ProjectNodeCreatedEvent.class));
    }

    @Test
    @DisplayName("노드 생성 성공 - 두 번째 노드 (nodeOrder 계산)")
    void createNode_Success_SecondNode() {
        // given
        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeValidator.validateProjectAndDevMember(anyLong(), anyLong()))
                .willReturn(testProject);
        given(projectNodeService.findMaxNodeOrderByProjectId(anyLong())).willReturn(1); // 기존 노드 있음

        ProjectNode secondNode = ProjectNode.builder()
                .projectNodeId(2L)
                .projectId(100L)
                .title("테스트 노드")
                .nodeOrder(2) // 두 번째 노드
                .build();

        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(secondNode);

        // when
        CreateNodeResponse response = createProjectNodeService.createNode(100L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.projectNodeId()).isEqualTo(2L);

        verify(projectNodeService).findMaxNodeOrderByProjectId(100L);
        verify(eventPublisher).publishEvent(any(ProjectNodeCreatedEvent.class));
    }

    @Test
    @DisplayName("노드 생성 시 프로젝트 상태가 IN_PROGRESS로 변경됨")
    void createNode_ProjectStatusChangedToInProgress() {
        // given
        Project contractProject = Project.builder()
                .projectId(100L)
                .projectTitle("테스트 프로젝트")
                .status(Status.CONTRACT)
                .build();

        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeValidator.validateProjectAndDevMember(anyLong(), anyLong()))
                .willReturn(contractProject);
        given(projectNodeService.findMaxNodeOrderByProjectId(anyLong())).willReturn(0);
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(testNode);

        // when
        createProjectNodeService.createNode(100L, request);

        // then
        assertThat(contractProject.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }
}
