package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.userTable.entity.UserTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static com.workhub.userTable.entity.UserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateProjectNodeServiceTest {

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private CreateProjectNodeService createProjectNodeService;

    private CreateNodeRequest mockRequest;

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

        mockRequest = new CreateNodeRequest(
                "새 노드",
                "노드 설명",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                Priority.MEDIUM
        );
    }

    @Test
    @DisplayName("빈 프로젝트에 첫 번째 노드를 생성하면 순서 조정 없이 노드가 생성된다.")
    void givenEmptyProject_whenCreateNode_thenCreateNodeWithoutAdjustment() {
        Long projectId = 100L;
        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("새 노드")
                .description("노드 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId)).thenReturn(Collections.emptyList());
        when(projectNodeService.saveProjectNode(any(ProjectNode.class))).thenReturn(savedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse result = createProjectNodeService.createNode(projectId, mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.projectNodeId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("새 노드");
        assertThat(result.nodeOrder()).isEqualTo(1);
        assertThat(result.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);

        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(1L),
                eq(ActionType.CREATE),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("기존 노드가 있는 프로젝트에 노드를 추가하면 마지막 순서 + 1로 생성된다.")
    void givenProjectWithNodes_whenCreateNode_thenCreateNodeWithNextOrder() {
        Long projectId = 100L;
        ProjectNode existingNode1 = ProjectNode.builder()
                .projectNodeId(1L)
                .nodeOrder(1)
                .build();

        ProjectNode existingNode2 = ProjectNode.builder()
                .projectNodeId(2L)
                .nodeOrder(2)
                .build();

        CreateNodeRequest newNodeRequest = new CreateNodeRequest(
                "새 노드",
                "새 노드 설명",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                Priority.MEDIUM
        );

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(3L)
                .projectId(projectId)
                .title("새 노드")
                .description("새 노드 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(3)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Arrays.asList(existingNode1, existingNode2));
        when(projectNodeService.saveProjectNode(any(ProjectNode.class))).thenReturn(savedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse result = createProjectNodeService.createNode(projectId, newNodeRequest);

        assertThat(result).isNotNull();
        assertThat(result.nodeOrder()).isEqualTo(3);
        assertThat(existingNode1.getNodeOrder()).isEqualTo(1); // 변경 없음
        assertThat(existingNode2.getNodeOrder()).isEqualTo(2); // 변경 없음

        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }


    @Test
    @DisplayName("노드 생성 시 히스토리가 올바르게 기록된다.")
    void givenCreateNodeRequest_whenCreateNode_thenRecordHistory() {
        Long projectId = 100L;
        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("새 노드")
                .description("노드 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId)).thenReturn(Collections.emptyList());
        when(projectNodeService.saveProjectNode(any(ProjectNode.class))).thenReturn(savedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        createProjectNodeService.createNode(projectId, mockRequest);

        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(1L),
                eq(ActionType.CREATE),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("노드 생성 시 ProjectNode.of 팩토리 메서드를 통해 엔티티가 생성된다.")
    void givenCreateNodeRequest_whenCreateNode_thenUseFactoryMethod() {
        Long projectId = 100L;
        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title(mockRequest.title())
                .description(mockRequest.description())
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId)).thenReturn(Collections.emptyList());
        when(projectNodeService.saveProjectNode(any(ProjectNode.class))).thenReturn(savedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse result = createProjectNodeService.createNode(projectId, mockRequest);

        assertThat(result).isNotNull();
        verify(projectNodeService).saveProjectNode(argThat(node ->
                node.getProjectId().equals(projectId) &&
                node.getTitle().equals(mockRequest.title()) &&
                node.getDescription().equals(mockRequest.description()) &&
                node.getNodeOrder().equals(1)
        ));
    }

    @Test
    @DisplayName("노드 생성 시 모든 메서드가 순차적으로 호출된다.")
    void givenCreateNodeRequest_whenCreateNode_thenAllMethodsCalledInOrder() {
        Long projectId = 100L;
        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("새 노드")
                .description("노드 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId)).thenReturn(Collections.emptyList());
        when(projectNodeService.saveProjectNode(any(ProjectNode.class))).thenReturn(savedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        createProjectNodeService.createNode(projectId, mockRequest);

        var inOrder = inOrder(projectNodeService, historyRecorder);
        inOrder.verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        inOrder.verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        inOrder.verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }
}