package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
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

import java.time.LocalDate;

import static com.workhub.userTable.entity.UserRole.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProjectNodeServiceTest {

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private DeleteProjectNodeService deleteProjectNodeService;

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
                .title("삭제할 노드")
                .description("삭제할 노드 설명")
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .contractStartDate(LocalDate.of(2024, 1, 1))
                .contractEndDate(LocalDate.of(2024, 12, 31))
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .projectId(100L)
                .build();
    }

    @Test
    @DisplayName("프로젝트 노드를 정상적으로 삭제하고 히스토리를 기록한다")
    void givenValidNodeId_whenDeleteProjectNode_thenSuccessAndRecordHistory() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.DELETED);

        verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectNodeId()).isEqualTo(nodeId);
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS); // 삭제 전 상태
        assertThat(capturedSnapshot.title()).isEqualTo("삭제할 노드");
    }

    @Test
    @DisplayName("존재하지 않는 노드 ID로 삭제 시 예외 발생")
    void givenInvalidNodeId_whenDeleteProjectNode_thenThrowException() {
        // given
        Long projectId = 100L;
        Long invalidNodeId = 999L;

        when(projectNodeService.findByIdAndProjectId(invalidNodeId, projectId))
                .thenThrow(new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() ->
                deleteProjectNodeService.deleteProjectNode(projectId, invalidNodeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeService).findByIdAndProjectId(invalidNodeId, projectId);
        verify(historyRecorder, never()).recordHistory(any(), any(), any(), any());
    }

    @Test
    @DisplayName("노드 삭제 시 삭제 전 상태가 히스토리에 기록된다")
    void givenNodeDeletion_whenDeleteProjectNode_thenRecordPreviousState() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;
        NodeStatus originalStatus = mockProjectNode.getNodeStatus();
        String originalTitle = mockProjectNode.getTitle();
        String originalDescription = mockProjectNode.getDescription();

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.projectNodeId()).isEqualTo(nodeId);
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(originalStatus);
        assertThat(capturedSnapshot.title()).isEqualTo(originalTitle);
        assertThat(capturedSnapshot.description()).isEqualTo(originalDescription);
    }

    @Test
    @DisplayName("노드 삭제 시 모든 메서드가 순차적으로 호출된다")
    void givenNodeDeletion_whenDeleteProjectNode_thenAllMethodsCalledInOrder() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        var inOrder = inOrder(projectNodeService, historyRecorder);
        inOrder.verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
        inOrder.verify(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));
    }

    @Test
    @DisplayName("노드 삭제 후 상태가 DELETED로 변경된다")
    void givenNodeDeletion_whenDeleteProjectNode_thenNodeStatusChangedToDeleted() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS); // 삭제 전

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        assertThat(mockProjectNode.getNodeStatus()).isEqualTo(NodeStatus.DELETED); // 삭제 후
        verify(projectNodeService).findByIdAndProjectId(nodeId, projectId);
    }

    @Test
    @DisplayName("다양한 상태의 노드를 삭제할 수 있다")
    void givenDifferentNodeStatus_whenDeleteProjectNode_thenSuccess() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;

        ProjectNode notStartedNode = ProjectNode.builder()
                .projectNodeId(nodeId)
                .title("미시작 노드")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .projectId(projectId)
                .build();

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(notStartedNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        assertThat(notStartedNode.getNodeStatus()).isEqualTo(NodeStatus.DELETED);

        ArgumentCaptor<NodeSnapshot> snapshotCaptor = ArgumentCaptor.forClass(NodeSnapshot.class);
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.DELETE),
                snapshotCaptor.capture()
        );

        NodeSnapshot capturedSnapshot = snapshotCaptor.getValue();
        assertThat(capturedSnapshot.nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED); // 삭제 전 상태
    }

    @Test
    @DisplayName("삭제 시 ActionType.DELETE로 히스토리가 기록된다")
    void givenNodeDeletion_whenDeleteProjectNode_thenRecordWithDeleteActionType() {
        // given
        Long projectId = 100L;
        Long nodeId = 1L;

        when(projectNodeService.findByIdAndProjectId(nodeId, projectId)).thenReturn(mockProjectNode);
        lenient().doNothing().when(historyRecorder).recordHistory(any(HistoryType.class), anyLong(), any(ActionType.class), any(Object.class));

        // when
        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);

        // then
        verify(historyRecorder).recordHistory(
                eq(HistoryType.PROJECT_NODE),
                eq(nodeId),
                eq(ActionType.DELETE),
                any(NodeSnapshot.class)
        );
    }
}
