package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
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
public class UpdateProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @InjectMocks
    UpdateProjectNodeService updateProjectNodeService;

    // ========== 성공 케이스 ==========

    @Test
    @DisplayName("노드 상태 변경이 성공하면 히스토리가 저장된다")
    void updateNodeStatus_success_shouldSaveHistory() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.NOT_STARTED)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when
        updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId);

        // then
        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService).updateNodeHistory(
                eq(nodeId),
                any(),
                eq("NOT_STARTED"),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    @Test
    @DisplayName("NOT_STARTED에서 IN_PROGRESS로 상태 변경이 성공한다")
    void updateNodeStatus_fromNotStartedToInProgress_shouldSucceed() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.NOT_STARTED)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when
        updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId);

        // then
        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("IN_PROGRESS에서 PENDING_REVIEW로 상태 변경이 성공한다")
    void updateNodeStatus_fromInProgressToPendingReview_shouldSucceed() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when
        updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId);

        // then
        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService).updateNodeHistory(anyLong(), any(), eq("IN_PROGRESS"), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("PENDING_REVIEW에서 ON_HOLD로 상태 변경이 성공한다")
    void updateNodeStatus_fromPendingReviewToOnHold_shouldSucceed() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.ON_HOLD);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.PENDING_REVIEW)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when
        updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId);

        // then
        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService).updateNodeHistory(anyLong(), any(), eq("PENDING_REVIEW"), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("올바른 사용자 정보로 히스토리가 저장된다")
    void updateNodeStatus_success_shouldSaveHistoryWithCorrectUserInfo() {
        // given
        Long nodeId = 10L;
        Long userId = 999L;
        String userIp = "10.0.0.1";
        String userAgent = "Mozilla/5.0";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.NOT_STARTED)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when
        updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId);

        // then
        verify(projectNodeService).updateNodeHistory(
                eq(nodeId),
                any(),
                anyString(),
                eq(userIp),
                eq(userAgent),
                eq(userId)
        );
    }

    // ========== 실패 케이스 ==========

    @Test
    @DisplayName("존재하지 않는 노드의 상태 변경 시 예외가 발생한다")
    void updateNodeStatus_whenNodeNotFound_shouldThrowException() {
        // given
        Long nodeId = 999L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        given(projectNodeService.findById(nodeId))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService, never()).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("동일한 상태로 변경 시도하면 예외가 발생한다")
    void updateNodeStatus_whenSameStatus_shouldThrowException() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATUS_ALREADY_SET);

        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService, never()).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("NOT_STARTED 상태를 NOT_STARTED로 변경 시도하면 예외가 발생한다")
    void updateNodeStatus_whenNotStartedToNotStarted_shouldThrowException() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.NOT_STARTED);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.NOT_STARTED)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATUS_ALREADY_SET);

        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService, never()).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("PENDING_REVIEW 상태를 PENDING_REVIEW로 변경 시도하면 예외가 발생한다")
    void updateNodeStatus_whenPendingReviewToPendingReview_shouldThrowException() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.PENDING_REVIEW)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATUS_ALREADY_SET);

        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService, never()).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("히스토리 저장 실패 시 예외가 발생한다")
    void updateNodeStatus_whenSaveHistoryFails_shouldThrowException() {
        // given
        Long nodeId = 1L;
        Long userId = 100L;
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        ProjectNode existingNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .nodeStatus(NodeStatus.NOT_STARTED)
                .build();

        given(projectNodeService.findById(nodeId)).willReturn(existingNode);
        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED))
                .when(projectNodeService).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeStatus(nodeId, request, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED);

        verify(projectNodeService).findById(nodeId);
        verify(projectNodeService).updateNodeHistory(anyLong(), any(), anyString(), anyString(), anyString(), anyLong());
    }
}