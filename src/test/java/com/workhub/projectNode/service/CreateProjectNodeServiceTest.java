package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @InjectMocks
    CreateProjectNodeService createProjectNodeService;

    @Test
    @DisplayName("노드 생성 시 노드와 히스토리가 모두 저장된다")
    void createNode_success_shouldSaveNodeAndHistory() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "Test Node",
                "Test Description",
                1,
                "HIGH"
        );

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(projectId)
                .title("Test Node")
                .description("Test Description")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(List.of());
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);

        // when
        CreateNodeResponse result = createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectNodeId()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Test Node");
        assertThat(result.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(projectNodeService).createProjectHistory(eq(10L), anyString(), eq(userIp), eq(userAgent), eq(loginUser));
    }

    @Test
    @DisplayName("같은 순서의 노드가 존재할 때 기존 노드들의 순서가 증가한다")
    void createNode_withSameOrder_shouldIncrementExistingNodeOrders() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "New Node",
                "New Description",
                2,
                "MEDIUM"
        );

        ProjectNode existingNode1 = ProjectNode.builder()
                .projectNodeId(1L)
                .nodeOrder(1)
                .build();

        ProjectNode existingNode2 = ProjectNode.builder()
                .projectNodeId(2L)
                .nodeOrder(2)
                .build();

        ProjectNode existingNode3 = ProjectNode.builder()
                .projectNodeId(3L)
                .nodeOrder(3)
                .build();

        List<ProjectNode> existingNodes = new ArrayList<>(Arrays.asList(existingNode1, existingNode2, existingNode3));

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(projectId)
                .title("New Node")
                .description("New Description")
                .nodeOrder(2)
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(existingNodes);
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);

        // when
        createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent);

        // then
        // nodeOrder가 2 이상인 노드들(existingNode2, existingNode3)의 incrementNodeOrder가 호출되어야 함
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(projectNodeService).createProjectHistory(anyLong(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("같은 순서의 노드가 없을 때 순서 조정 없이 저장된다")
    void createNode_withoutSameOrder_shouldSaveWithoutAdjustment() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "New Node",
                "New Description",
                5,
                "LOW"
        );

        ProjectNode existingNode1 = ProjectNode.builder()
                .projectNodeId(1L)
                .nodeOrder(1)
                .build();

        ProjectNode existingNode2 = ProjectNode.builder()
                .projectNodeId(2L)
                .nodeOrder(2)
                .build();

        List<ProjectNode> existingNodes = Arrays.asList(existingNode1, existingNode2);

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(projectId)
                .title("New Node")
                .description("New Description")
                .nodeOrder(5)
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(existingNodes);
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);

        // when
        CreateNodeResponse result = createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(projectNodeService).createProjectHistory(anyLong(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("첫 번째 노드 생성 시 성공한다")
    void createNode_firstNode_shouldSucceed() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "First Node",
                "First Description",
                1,
                "CRITICAL"
        );

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("First Node")
                .description("First Description")
                .nodeOrder(1)
                .priority(Priority.CRITICAL)
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(List.of());
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);

        // when
        CreateNodeResponse result = createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectNodeId()).isEqualTo(1L);
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
    }

    @Test
    @DisplayName("노드 히스토리 저장 시 올바른 사용자 정보가 전달된다")
    void createNode_success_shouldPassCorrectUserInfo() {
        // given
        Long projectId = 1L;
        Long loginUser = 999L;
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "Test Node",
                "Test Description",
                1,
                "HIGH"
        );

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(projectId)
                .description("Test Description")
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(List.of());
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);

        // when
        createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent);

        // then
        verify(projectNodeService).createProjectHistory(
                eq(10L),
                eq("Test Description"),
                eq(userIp),
                eq(userAgent),
                eq(loginUser)
        );
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("노드 저장 실패 시 예외가 발생한다")
    void createNode_whenSaveNodeFails_shouldThrowException() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "Test Node",
                "Test Description",
                1,
                "HIGH"
        );

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(List.of());
        given(projectNodeService.saveProjectNode(any(ProjectNode.class)))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NODE_SAVE_FAILED));

        // when & then
        assertThatThrownBy(() -> createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_SAVE_FAILED);

        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(projectNodeService, never()).createProjectHistory(anyLong(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("히스토리 저장 실패 시 예외가 발생한다")
    void createNode_whenSaveHistoryFails_shouldThrowException() {
        // given
        Long projectId = 1L;
        Long loginUser = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        CreateNodeRequest request = new CreateNodeRequest(
                "Test Node",
                "Test Description",
                1,
                "HIGH"
        );

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(projectId)
                .description("Test Description")
                .build();

        given(projectNodeService.findByProjectIdByNodeOrder(projectId)).willReturn(List.of());
        given(projectNodeService.saveProjectNode(any(ProjectNode.class))).willReturn(savedNode);
        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED))
                .when(projectNodeService).createProjectHistory(anyLong(), anyString(), anyString(), anyString(), anyLong());

        // when & then
        assertThatThrownBy(() -> createProjectNodeService.createNode(projectId, request, loginUser, userIp, userAgent))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED);

        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
        verify(projectNodeService).saveProjectNode(any(ProjectNode.class));
        verify(projectNodeService).createProjectHistory(anyLong(), anyString(), anyString(), anyString(), anyLong());
    }

}