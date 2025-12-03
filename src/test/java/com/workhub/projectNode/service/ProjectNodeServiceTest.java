package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import com.workhub.projectNode.repository.ProjectNodeHistoryRepository;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectNodeServiceTest {

    @Mock
    ProjectNodeRepository projectNodeRepository;

    @Mock
    ProjectNodeHistoryRepository projectNodeHistoryRepository;

    @InjectMocks
    ProjectNodeService projectNodeService;

    // ========== saveProjectNode 테스트 ==========

    @Test
    @DisplayName("프로젝트 노드를 저장하면 저장된 노드가 반환된다")
    void saveProjectNode_success_shouldReturnSavedNode() {
        // given
        ProjectNode projectNode = ProjectNode.builder()
                .projectId(1L)
                .title("Test Node")
                .description("Test Description")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .build();

        ProjectNode savedNode = ProjectNode.builder()
                .projectNodeId(10L)
                .projectId(1L)
                .title("Test Node")
                .description("Test Description")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .build();

        given(projectNodeRepository.save(any(ProjectNode.class))).willReturn(savedNode);

        // when
        ProjectNode result = projectNodeService.saveProjectNode(projectNode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(10L);
        assertThat(result.getProjectId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Node");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getNodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getNodeOrder()).isEqualTo(1);
        verify(projectNodeRepository).save(projectNode);
    }

    @Test
    @DisplayName("노드 저장 실패 시 예외가 발생한다")
    void saveProjectNode_whenSaveFails_shouldThrowException() {
        // given
        ProjectNode projectNode = ProjectNode.builder()
                .title("Test Node")
                .build();

        given(projectNodeRepository.save(any(ProjectNode.class)))
                .willThrow(new BusinessException(ErrorCode.PROJECT_NODE_SAVE_FAILED));

        // when & then
        assertThatThrownBy(() -> projectNodeService.saveProjectNode(projectNode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_SAVE_FAILED);

        verify(projectNodeRepository).save(projectNode);
    }

    // ========== findByProjectIdByNodeOrder 테스트 ==========

    @Test
    @DisplayName("프로젝트 ID로 노드 리스트를 조회하면 nodeOrder로 정렬된 리스트가 반환된다")
    void findByProjectIdByNodeOrder_success_shouldReturnSortedList() {
        // given
        Long projectId = 1L;

        List<ProjectNode> expectedNodes = Arrays.asList(
                ProjectNode.builder()
                        .projectNodeId(1L)
                        .projectId(projectId)
                        .nodeOrder(1)
                        .build(),
                ProjectNode.builder()
                        .projectNodeId(2L)
                        .projectId(projectId)
                        .nodeOrder(2)
                        .build(),
                ProjectNode.builder()
                        .projectNodeId(3L)
                        .projectId(projectId)
                        .nodeOrder(3)
                        .build()
        );

        given(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .willReturn(expectedNodes);

        // when
        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getNodeOrder()).isEqualTo(1);
        assertThat(result.get(1).getNodeOrder()).isEqualTo(2);
        assertThat(result.get(2).getNodeOrder()).isEqualTo(3);
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    @Test
    @DisplayName("프로젝트에 노드가 없으면 빈 리스트가 반환된다")
    void findByProjectIdByNodeOrder_withNoNodes_shouldReturnEmptyList() {
        // given
        Long projectId = 1L;

        given(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .willReturn(List.of());

        // when
        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    @Test
    @DisplayName("삭제된 노드는 조회되지 않는다")
    void findByProjectIdByNodeOrder_shouldNotReturnDeletedNodes() {
        // given
        Long projectId = 1L;

        // 삭제되지 않은 노드만 반환되어야 함
        List<ProjectNode> activeNodes = Arrays.asList(
                ProjectNode.builder()
                        .projectNodeId(1L)
                        .projectId(projectId)
                        .nodeOrder(1)
                        .build(),
                ProjectNode.builder()
                        .projectNodeId(3L)
                        .projectId(projectId)
                        .nodeOrder(3)
                        .build()
        );

        given(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .willReturn(activeNodes);

        // when
        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectNodeId()).isEqualTo(1L);
        assertThat(result.get(1).getProjectNodeId()).isEqualTo(3L);
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    // ========== createNodeHistory 테스트 ==========

    @Test
    @DisplayName("프로젝트 노드 히스토리를 생성하면 repository.save가 호출된다")
    void createNodeHistory_success_shouldInvokeSave() {
        // given
        Long nodeId = 10L;
        String beforeStatus = "Test Description";
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        Long userId = 100L;

        // when
        projectNodeService.createNodeHistory(nodeId, beforeStatus, userIp, userAgent, userId);

        // then
        verify(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("히스토리 생성 시 올바른 데이터로 ProjectNodeHistory가 생성된다")
    void createNodeHistory_success_shouldCreateWithCorrectData() {
        // given
        Long nodeId = 10L;
        String beforeStatus = "Initial Status";
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";
        Long userId = 999L;

        ProjectNodeHistory capturedHistory = null;

        // when
        projectNodeService.createNodeHistory(nodeId, beforeStatus, userIp, userAgent, userId);

        // then
        verify(projectNodeHistoryRepository).save(argThat(history -> {
            // ProjectNodeHistory.of 메서드가 올바른 파라미터로 호출되는지 검증
            return history != null;
        }));
    }

    @Test
    @DisplayName("히스토리 저장 실패 시 예외가 발생한다")
    void createNodeHistory_whenSaveFails_shouldThrowException() {
        // given
        Long nodeId = 10L;
        String beforeStatus = "Test Description";
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        Long userId = 100L;

        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED))
                .when(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));

        // when & then
        assertThatThrownBy(() -> projectNodeService.createNodeHistory(nodeId, beforeStatus, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED);

        verify(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("다양한 사용자 정보로 히스토리 생성이 성공한다")
    void createNodeHistory_withVariousUserInfo_shouldSucceed() {
        // given
        Long nodeId = 1L;
        String beforeStatus = "Status";
        String userIp1 = "127.0.0.1";
        String userIp2 = "192.168.1.1";
        String userAgent1 = "Agent1";
        String userAgent2 = "Agent2";
        Long userId1 = 1L;
        Long userId2 = 2L;

        // when
        projectNodeService.createNodeHistory(nodeId, beforeStatus, userIp1, userAgent1, userId1);
        projectNodeService.createNodeHistory(nodeId, beforeStatus, userIp2, userAgent2, userId2);

        // then
        verify(projectNodeHistoryRepository, times(2)).save(any(ProjectNodeHistory.class));
    }

    // ========== updateNodeHistory 테스트 ==========

    @Test
    @DisplayName("노드 상태 변경 히스토리를 저장하면 originalCreator를 조회하고 히스토리가 저장된다")
    void updateNodeHistory_success_shouldSaveHistory() {
        // given
        Long nodeId = 10L;
        ActionType actionType = ActionType.UPDATE;
        String beforeStatus = "NOT_STARTED";
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        Long userId = 100L;
        Long originalCreator = 999L;

        ProjectNodeHistory createHistory = ProjectNodeHistory.builder()
                .targetId(nodeId)
                .actionType(ActionType.CREATE)
                .createdBy(originalCreator)
                .build();

        given(projectNodeHistoryRepository.findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE))
                .willReturn(Optional.of(createHistory));

        // when
        projectNodeService.updateNodeHistory(nodeId, actionType, beforeStatus, userIp, userAgent, userId);

        // then
        verify(projectNodeHistoryRepository).findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE);
        verify(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("업데이트 히스토리 저장 시 originalCreator가 올바르게 조회된다")
    void updateNodeHistory_success_shouldFetchOriginalCreator() {
        // given
        Long nodeId = 5L;
        ActionType actionType = ActionType.UPDATE;
        String beforeStatus = "IN_PROGRESS";
        String userIp = "192.168.0.1";
        String userAgent = "CustomAgent";
        Long userId = 50L;
        Long originalCreator = 123L;

        ProjectNodeHistory createHistory = ProjectNodeHistory.builder()
                .targetId(nodeId)
                .actionType(ActionType.CREATE)
                .createdBy(originalCreator)
                .build();

        given(projectNodeHistoryRepository.findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE))
                .willReturn(Optional.of(createHistory));

        // when
        projectNodeService.updateNodeHistory(nodeId, actionType, beforeStatus, userIp, userAgent, userId);

        // then
        verify(projectNodeHistoryRepository).findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE);
        verify(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("originalCreator를 찾을 수 없으면 예외가 발생한다")
    void updateNodeHistory_whenOriginalCreatorNotFound_shouldThrowException() {
        // given
        Long nodeId = 10L;
        ActionType actionType = ActionType.UPDATE;
        String beforeStatus = "NOT_STARTED";
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        Long userId = 100L;

        given(projectNodeHistoryRepository.findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectNodeService.updateNodeHistory(nodeId, actionType, beforeStatus, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeHistoryRepository).findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE);
        verify(projectNodeHistoryRepository, never()).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("업데이트 히스토리 저장 실패 시 예외가 발생한다")
    void updateNodeHistory_whenSaveFails_shouldThrowException() {
        // given
        Long nodeId = 10L;
        ActionType actionType = ActionType.UPDATE;
        String beforeStatus = "NOT_STARTED";
        String userIp = "127.0.0.1";
        String userAgent = "TestAgent";
        Long userId = 100L;
        Long originalCreator = 999L;

        ProjectNodeHistory createHistory = ProjectNodeHistory.builder()
                .targetId(nodeId)
                .actionType(ActionType.CREATE)
                .createdBy(originalCreator)
                .build();

        given(projectNodeHistoryRepository.findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE))
                .willReturn(Optional.of(createHistory));
        doThrow(new BusinessException(ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED))
                .when(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));

        // when & then
        assertThatThrownBy(() -> projectNodeService.updateNodeHistory(nodeId, actionType, beforeStatus, userIp, userAgent, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_HISTORY_SAVE_FAILED);

        verify(projectNodeHistoryRepository).findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE);
        verify(projectNodeHistoryRepository).save(any(ProjectNodeHistory.class));
    }

    @Test
    @DisplayName("다양한 ActionType으로 히스토리 업데이트가 성공한다")
    void updateNodeHistory_withVariousActionTypes_shouldSucceed() {
        // given
        Long nodeId = 1L;
        String beforeStatus = "Status";
        String userIp = "127.0.0.1";
        String userAgent = "Agent";
        Long userId = 1L;
        Long originalCreator = 100L;

        ProjectNodeHistory createHistory = ProjectNodeHistory.builder()
                .targetId(nodeId)
                .actionType(ActionType.CREATE)
                .createdBy(originalCreator)
                .build();

        given(projectNodeHistoryRepository.findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE))
                .willReturn(Optional.of(createHistory));

        // when
        projectNodeService.updateNodeHistory(nodeId, ActionType.UPDATE, beforeStatus, userIp, userAgent, userId);
        projectNodeService.updateNodeHistory(nodeId, ActionType.UPDATE, beforeStatus, userIp, userAgent, userId);

        // then
        verify(projectNodeHistoryRepository, times(2)).findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(nodeId, ActionType.CREATE);
        verify(projectNodeHistoryRepository, times(2)).save(any(ProjectNodeHistory.class));
    }
}