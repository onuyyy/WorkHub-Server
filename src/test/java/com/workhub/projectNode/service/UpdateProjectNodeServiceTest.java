package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
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

import static com.workhub.userTable.entity.UserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProjectNodeServiceTest {

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private UpdateProjectNodeService updateProjectNodeService;

    private ProjectNode mockProjectNode;

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

        mockProjectNode = ProjectNode.builder()
                .projectNodeId(1L)
                .title("테스트 노드")
                .description("테스트 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .projectId(100L)
                .build();
    }

    @Test
    @DisplayName("프로젝트 노드 상태를 정상적으로 업데이트하고 히스토리를 기록한다.")
    void givenValidNodeIdAndStatus_whenUpdateNodeStatus_thenSuccess() {
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
        verify(projectNodeService).findById(nodeId);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("NOT_STARTED")
        );
    }

    @Test
    @DisplayName("존재하지 않는 노드 ID로 상태 업데이트 시 예외 발생")
    void givenInvalidNodeId_whenUpdateNodeStatus_thenThrowException() {
        Long invalidNodeId = 999L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findById(invalidNodeId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        assertThatThrownBy(() ->
                updateProjectNodeService.updateNodeStatus(invalidNodeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeService).findById(invalidNodeId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경 시도 시 예외 발생")
    void givenSameStatus_whenUpdateNodeStatus_thenThrowException() {
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.NOT_STARTED);

        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);

        assertThatThrownBy(() ->
                updateProjectNodeService.updateNodeStatus(nodeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATUS_ALREADY_SET);

        verify(projectNodeService).findById(nodeId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("NOT_STARTED에서 PENDING_REVIEW로 상태 변경 시 성공")
    void givenNotStartedStatus_whenUpdateToPendingReview_thenSuccess() {
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.PENDING_REVIEW);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("NOT_STARTED")
        );
    }

    @Test
    @DisplayName("IN_PROGRESS에서 PENDING_REVIEW로 상태 변경 시 성공")
    void givenInProgressStatus_whenUpdateToPendingReview_thenSuccess() {
        Long nodeId = 1L;
        ProjectNode inProgressNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .title("진행중 노드")
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .build();

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        when(projectNodeService.findById(nodeId)).thenReturn(inProgressNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        assertThat(inProgressNode.getNodeStatus()).isEqualTo(NodeStatus.PENDING_REVIEW);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("IN_PROGRESS")
        );
    }

    @Test
    @DisplayName("ON_HOLD에서 IN_PROGRESS로 상태 변경 시 성공")
    void givenOnHoldStatus_whenUpdateToInProgress_thenSuccess() {
        Long nodeId = 1L;
        ProjectNode onHoldNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .title("보류된 노드")
                .nodeStatus(NodeStatus.ON_HOLD)
                .build();

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findById(nodeId)).thenReturn(onHoldNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        assertThat(onHoldNode.getNodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("ON_HOLD")
        );
    }

    @Test
    @DisplayName("상태 업데이트 시 변경 전 상태가 히스토리에 기록된다.")
    void givenStatusChange_whenUpdateNodeStatus_thenRecordPreviousStatus() {
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("NOT_STARTED") // 변경 전 상태
        );
    }

    @Test
    @DisplayName("상태 업데이트 시 모든 메서드가 순차적으로 호출된다.")
    void givenStatusChange_whenUpdateNodeStatus_thenAllMethodsCalledInOrder() {
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, request);

        var inOrder = inOrder(projectNodeService, historyRecorder);
        inOrder.verify(projectNodeService).findById(nodeId);
        inOrder.verify(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("여러 번 상태를 변경해도 각각 히스토리가 기록된다.")
    void givenMultipleStatusChanges_whenUpdateNodeStatus_thenEachChangeRecorded() {
        Long nodeId = 1L;

        // 첫 번째 변경: NOT_STARTED -> IN_PROGRESS
        UpdateNodeStatusRequest firstRequest = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);
        when(projectNodeService.findById(nodeId)).thenReturn(mockProjectNode);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, firstRequest);

        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("NOT_STARTED")
        );

        // 두 번째 변경: IN_PROGRESS -> PENDING_REVIEW
        UpdateNodeStatusRequest secondRequest = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);
        reset(historyRecorder);
        doNothing().when(historyRecorder).recordHistory(any(), anyLong(), any(), anyString());

        updateProjectNodeService.updateNodeStatus(nodeId, secondRequest);

        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                eq("IN_PROGRESS")
        );
    }
}