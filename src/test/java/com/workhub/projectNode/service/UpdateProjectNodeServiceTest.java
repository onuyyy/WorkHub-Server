package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.dto.UpdateNodeRequest;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;

import java.time.LocalDate;
import com.workhub.userTable.entity.UserTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.lenient;

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
        Long projectId = 100L;
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
        verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(capturedSnapshot.projectNodeId()).isEqualTo(nodeId);
    }

    @Test
    @DisplayName("존재하지 않는 노드 ID로 상태 업데이트 시 예외 발생")
    void givenInvalidNodeId_whenUpdateNodeStatus_thenThrowException() {
        Long projectId = 100L;
        Long invalidNodeId = 999L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findByIdAndProjectId(invalidNodeId, projectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        assertThatThrownBy(() ->
                updateProjectNodeService.updateNodeStatus(projectId, invalidNodeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeService).findByIdAndProjectId(invalidNodeId, projectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("현재 상태와 동일한 상태로 변경 시도 시 예외 발생")
    void givenSameStatus_whenUpdateNodeStatus_thenThrowException() {
        Long projectId = 100L;
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.NOT_STARTED);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);

        assertThatThrownBy(() ->
                updateProjectNodeService.updateNodeStatus(projectId, nodeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATUS_ALREADY_SET);

        verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("NOT_STARTED에서 PENDING_REVIEW로 상태 변경 시 성공")
    void givenNotStartedStatus_whenUpdateToPendingReview_thenSuccess() {
        Long projectId = 100L;
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.PENDING_REVIEW);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("IN_PROGRESS에서 PENDING_REVIEW로 상태 변경 시 성공")
    void givenInProgressStatus_whenUpdateToPendingReview_thenSuccess() {
        Long projectId = 100L;
        Long nodeId = 1L;
        ProjectNode inProgressNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .title("진행중 노드")
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .projectId(projectId)
                .build();

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(inProgressNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        assertThat(inProgressNode.getNodeStatus()).isEqualTo(NodeStatus.PENDING_REVIEW);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("ON_HOLD에서 IN_PROGRESS로 상태 변경 시 성공")
    void givenOnHoldStatus_whenUpdateToInProgress_thenSuccess() {
        Long projectId = 100L;
        Long nodeId = 1L;
        ProjectNode onHoldNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .title("보류된 노드")
                .nodeStatus(NodeStatus.ON_HOLD)
                .projectId(projectId)
                .build();

        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(onHoldNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        assertThat(onHoldNode.getNodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.ON_HOLD);
    }

    @Test
    @DisplayName("상태 업데이트 시 변경 전 상태가 히스토리에 기록된다.")
    void givenStatusChange_whenUpdateNodeStatus_thenRecordPreviousStatus() {
        Long projectId = 100L;
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED); // 변경 전 상태
    }

    @Test
    @DisplayName("상태 업데이트 시 모든 메서드가 순차적으로 호출된다.")
    void givenStatusChange_whenUpdateNodeStatus_thenAllMethodsCalledInOrder() {
        Long projectId = 100L;
        Long nodeId = 1L;
        UpdateNodeStatusRequest request = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);

        var inOrder = inOrder(projectNodeService, historyRecorder);
        inOrder.verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
        inOrder.verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }

    @Test
    @DisplayName("여러 번 상태를 변경해도 각각 히스토리가 기록된다.")
    void givenMultipleStatusChanges_whenUpdateNodeStatus_thenEachChangeRecorded() {
        Long projectId = 100L;
        Long nodeId = 1L;

        // 첫 번째 변경: NOT_STARTED -> IN_PROGRESS
        UpdateNodeStatusRequest firstRequest = new UpdateNodeStatusRequest(NodeStatus.IN_PROGRESS);
        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, firstRequest);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot firstSnapshot = snapshotCaptor.getValue();
        assertThat(firstSnapshot.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);

        // 두 번째 변경: IN_PROGRESS -> PENDING_REVIEW
        UpdateNodeStatusRequest secondRequest = new UpdateNodeStatusRequest(NodeStatus.PENDING_REVIEW);
        reset(historyRecorder);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, secondRequest);

        ArgumentCaptor<NodeSnapshot> secondSnapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                secondSnapshotCaptor.capture()
        );

        NodeSnapshot secondSnapshot = secondSnapshotCaptor.getValue();
        assertThat(secondSnapshot.nodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("프로젝트 노드 정보를 모두 업데이트하고 응답을 반환한다.")
    void givenAllFields_whenUpdateNode_thenUpdateAllFieldsAndReturnResponse() {
        Long projectId = 100L;
        Long nodeId = 1L;
        LocalDate newStartDate = LocalDate.of(2024, 1, 1);
        LocalDate newEndDate = LocalDate.of(2024, 12, 31);

        UpdateNodeRequest request = new UpdateNodeRequest(
                "수정된 제목",
                "수정된 설명",
                newStartDate,
                newEndDate,
                Priority.HIGH
        );

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse response = updateProjectNodeService.updateNode(projectId, nodeId, request);

        assertThat(mockProjectNode.getTitle()).isEqualTo("수정된 제목");
        assertThat(mockProjectNode.getDescription()).isEqualTo("수정된 설명");
        assertThat(mockProjectNode.getContractStartDate()).isEqualTo(newStartDate);
        assertThat(mockProjectNode.getContractEndDate()).isEqualTo(newEndDate);
        assertThat(mockProjectNode.getPriority()).isEqualTo(Priority.HIGH);

        assertThat(response.projectNodeId()).isEqualTo(nodeId);
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.description()).isEqualTo("수정된 설명");
        assertThat(response.startDate()).isEqualTo(newStartDate);
        assertThat(response.endDate()).isEqualTo(newEndDate);
        assertThat(response.priority()).isEqualTo(Priority.HIGH);

        verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
    }

    @Test
    @DisplayName("프로젝트 노드의 일부 필드만 업데이트한다.")
    void givenPartialFields_whenUpdateNode_thenUpdateOnlyNonNullFields() {
        Long projectId = 100L;
        Long nodeId = 1L;
        String originalDescription = mockProjectNode.getDescription();

        UpdateNodeRequest request = new UpdateNodeRequest(
                "새로운 제목",
                null,  // description은 업데이트하지 않음
                null,  // startDate는 업데이트하지 않음
                null,  // endDate는 업데이트하지 않음
                Priority.CRITICAL
        );

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse response = updateProjectNodeService.updateNode(projectId, nodeId, request);

        assertThat(mockProjectNode.getTitle()).isEqualTo("새로운 제목");
        assertThat(mockProjectNode.getDescription()).isEqualTo(originalDescription); // 변경되지 않음
        assertThat(mockProjectNode.getContractStartDate()).isNull(); // 변경되지 않음
        assertThat(mockProjectNode.getContractEndDate()).isNull(); // 변경되지 않음
        assertThat(mockProjectNode.getPriority()).isEqualTo(Priority.CRITICAL);

        assertThat(response.title()).isEqualTo("새로운 제목");
        assertThat(response.description()).isEqualTo(originalDescription);
    }

    @Test
    @DisplayName("프로젝트 노드 업데이트 시 변경 전 상태가 히스토리에 기록된다.")
    void givenNodeUpdate_whenUpdateNode_thenRecordPreviousStateInHistory() {
        Long projectId = 100L;
        Long nodeId = 1L;
        String originalTitle = mockProjectNode.getTitle();
        String originalDescription = mockProjectNode.getDescription();

        UpdateNodeRequest request = new UpdateNodeRequest(
                "수정된 제목",
                "수정된 설명",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                Priority.HIGH
        );

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNode(projectId, nodeId, request);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.UPDATE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectNodeId()).isEqualTo(nodeId);
        assertThat(capturedSnapshot.title()).isEqualTo(originalTitle);
        assertThat(capturedSnapshot.description()).isEqualTo(originalDescription);
    }

    @Test
    @DisplayName("존재하지 않는 노드 ID로 업데이트 시 예외 발생")
    void givenInvalidNodeId_whenUpdateNode_thenThrowException() {
        Long projectId = 100L;
        Long invalidNodeId = 999L;

        UpdateNodeRequest request = new UpdateNodeRequest(
                "수정된 제목",
                "수정된 설명",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                Priority.HIGH
        );

        when(projectNodeService.findByIdAndProjectId(invalidNodeId, projectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        assertThatThrownBy(() ->
                updateProjectNodeService.updateNode(projectId, invalidNodeId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeService).findByIdAndProjectId(invalidNodeId, projectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("노드 업데이트 시 모든 메서드가 순차적으로 호출된다.")
    void givenNodeUpdate_whenUpdateNode_thenAllMethodsCalledInOrder() {
        Long projectId = 100L;
        Long nodeId = 1L;

        UpdateNodeRequest request = new UpdateNodeRequest(
                "수정된 제목",
                "수정된 설명",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                Priority.HIGH
        );

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        updateProjectNodeService.updateNode(projectId, nodeId, request);

        var inOrder = inOrder(projectNodeService, historyRecorder);
        inOrder.verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
        inOrder.verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }

    @Test
    @DisplayName("노드 업데이트 후 반환된 응답에 모든 필드가 포함된다.")
    void givenNodeUpdate_whenUpdateNode_thenReturnCompleteResponse() {
        Long projectId = 100L;
        Long nodeId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        UpdateNodeRequest request = new UpdateNodeRequest(
                "새 제목",
                "새 설명",
                startDate,
                endDate,
                Priority.MEDIUM
        );

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        CreateNodeResponse response = updateProjectNodeService.updateNode(projectId, nodeId, request);

        assertThat(response).isNotNull();
        assertThat(response.projectNodeId()).isEqualTo(nodeId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(response.description()).isEqualTo("새 설명");
        assertThat(response.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);
        assertThat(response.nodeOrder()).isEqualTo(1);
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
    }
}