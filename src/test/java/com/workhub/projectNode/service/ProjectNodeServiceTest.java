package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.entity.ConfirmStatus;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectNodeServiceTest {

    @Mock
    private ProjectNodeRepository projectNodeRepository;

    @InjectMocks
    private ProjectNodeService projectNodeService;

    private ProjectNode mockProjectNode;

    @BeforeEach
    void init() {
        mockProjectNode = ProjectNode.builder()
                .projectNodeId(1L)
                .title("테스트 노드")
                .description("테스트 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.MEDIUM)
                .confirmStatus(ConfirmStatus.PENDING)
                .nodeOrder(1)
                .projectId(100L)
                .userId(1L)
                .build();
    }

    @Test
    @DisplayName("프로젝트 노드를 저장하면 저장된 노드를 반환한다.")
    void givenProjectNode_whenSaveProjectNode_thenReturnSavedNode() {
        when(projectNodeRepository.save(any(ProjectNode.class))).thenReturn(mockProjectNode);

        ProjectNode result = projectNodeService.saveProjectNode(mockProjectNode);

        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 노드");
        assertThat(result.getNodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        verify(projectNodeRepository).save(mockProjectNode);
    }

    @Test
    @DisplayName("프로젝트 ID로 노드를 조회하면 순서대로 정렬된 리스트를 반환한다.")
    void givenProjectId_whenFindByProjectIdByNodeOrder_thenReturnSortedList() {
        Long projectId = 100L;
        ProjectNode node1 = ProjectNode.builder()
                .projectNodeId(1L)
                .title("노드 1")
                .nodeOrder(1)
                .projectId(projectId)
                .build();

        ProjectNode node2 = ProjectNode.builder()
                .projectNodeId(2L)
                .title("노드 2")
                .nodeOrder(2)
                .projectId(projectId)
                .build();

        ProjectNode node3 = ProjectNode.builder()
                .projectNodeId(3L)
                .title("노드 3")
                .nodeOrder(3)
                .projectId(projectId)
                .build();

        List<ProjectNode> mockNodes = Arrays.asList(node1, node2, node3);
        when(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .thenReturn(mockNodes);

        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getNodeOrder()).isEqualTo(1);
        assertThat(result.get(1).getNodeOrder()).isEqualTo(2);
        assertThat(result.get(2).getNodeOrder()).isEqualTo(3);
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    @Test
    @DisplayName("프로젝트 ID로 노드를 조회했을 때 노드가 없으면 빈 리스트를 반환한다.")
    void givenProjectIdWithNoNodes_whenFindByProjectIdByNodeOrder_thenReturnEmptyList() {
        Long projectId = 999L;
        when(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .thenReturn(Collections.emptyList());

        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    @Test
    @DisplayName("존재하는 프로젝트 노드 ID로 조회하면 노드를 반환한다.")
    void givenValidNodeId_whenFindById_thenReturnProjectNode() {
        Long nodeId = 1L;
        when(projectNodeRepository.findById(nodeId)).thenReturn(Optional.of(mockProjectNode));

        ProjectNode result = projectNodeService.findById(nodeId);

        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(nodeId);
        assertThat(result.getTitle()).isEqualTo("테스트 노드");
        verify(projectNodeRepository).findById(nodeId);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 노드 ID로 조회하면 예외를 발생시킨다.")
    void givenInvalidNodeId_whenFindById_thenThrowException() {
        Long nodeId = 999L;
        when(projectNodeRepository.findById(nodeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectNodeService.findById(nodeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);

        verify(projectNodeRepository).findById(nodeId);
    }

    @Test
    @DisplayName("삭제된 노드는 프로젝트 ID로 조회 시 포함되지 않는다.")
    void givenProjectIdWithDeletedNodes_whenFindByProjectIdByNodeOrder_thenReturnOnlyNonDeletedNodes() {
        Long projectId = 100L;
        ProjectNode activeNode = ProjectNode.builder()
                .projectNodeId(1L)
                .title("활성 노드")
                .nodeOrder(1)
                .projectId(projectId)
                .build();

        // 삭제되지 않은 노드만 반환
        when(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId))
                .thenReturn(Collections.singletonList(activeNode));

        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(projectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("활성 노드");
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    @Test
    @DisplayName("여러 프로젝트의 노드가 있을 때 특정 프로젝트 ID로 조회하면 해당 프로젝트의 노드만 반환한다.")
    void givenMultipleProjects_whenFindByProjectIdByNodeOrder_thenReturnOnlyMatchingProjectNodes() {
        Long targetProjectId = 100L;
        ProjectNode node1 = ProjectNode.builder()
                .projectNodeId(1L)
                .title("프로젝트 100 노드 1")
                .nodeOrder(1)
                .projectId(targetProjectId)
                .build();

        ProjectNode node2 = ProjectNode.builder()
                .projectNodeId(2L)
                .title("프로젝트 100 노드 2")
                .nodeOrder(2)
                .projectId(targetProjectId)
                .build();

        when(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(targetProjectId))
                .thenReturn(Arrays.asList(node1, node2));

        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(targetProjectId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(node -> node.getProjectId().equals(targetProjectId));
        verify(projectNodeRepository).findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(targetProjectId);
    }
}