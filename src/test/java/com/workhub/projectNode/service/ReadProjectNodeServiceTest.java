package com.workhub.projectNode.service;

import com.workhub.global.history.HistoryRecorder;
import com.workhub.projectNode.dto.NodeListResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadProjectNodeServiceTest {

    @Mock
    private ProjectNodeService projectNodeService;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private ReadProjectNodeService readProjectNodeService;

    @Test
    @DisplayName("프로젝트에 노드가 없는 경우 빈 리스트를 반환한다")
    void givenEmptyProject_whenGetNodeList_thenReturnEmptyList() {
        // given
        Long projectId = 100L;
        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Collections.emptyList());

        // when
        List<NodeListResponse> result = readProjectNodeService.getNodeListByProject(projectId);

        // then
        assertThat(result).isEmpty();
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
    }

    @Test
    @DisplayName("프로젝트에 노드가 1개 있는 경우 1개의 응답을 반환한다")
    void givenProjectWithOneNode_whenGetNodeList_thenReturnOneNode() {
        // given
        Long projectId = 100L;
        ProjectNode node = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("노드 1")
                .description("노드 1 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .contractStartDate(LocalDate.of(2025, 1, 1))
                .contractEndDate(LocalDate.of(2025, 12, 31))
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Collections.singletonList(node));

        // when
        List<NodeListResponse> result = readProjectNodeService.getNodeListByProject(projectId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).projectNodeId()).isEqualTo(1L);
        assertThat(result.get(0).title()).isEqualTo("노드 1");
        assertThat(result.get(0).description()).isEqualTo("노드 1 설명");
        assertThat(result.get(0).nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(result.get(0).priority()).isEqualTo(Priority.HIGH);
        assertThat(result.get(0).nodeOrder()).isEqualTo(1);
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
    }

    @Test
    @DisplayName("프로젝트에 여러 노드가 있는 경우 nodeOrder 순서대로 정렬되어 반환된다")
    void givenProjectWithMultipleNodes_whenGetNodeList_thenReturnSortedByNodeOrder() {
        // given
        Long projectId = 100L;
        ProjectNode node1 = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("노드 1")
                .description("노드 1 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .contractStartDate(LocalDate.of(2025, 1, 1))
                .contractEndDate(LocalDate.of(2025, 3, 31))
                .build();

        ProjectNode node2 = ProjectNode.builder()
                .projectNodeId(2L)
                .projectId(projectId)
                .title("노드 2")
                .description("노드 2 설명")
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .priority(Priority.MEDIUM)
                .nodeOrder(2)
                .contractStartDate(LocalDate.of(2025, 4, 1))
                .contractEndDate(LocalDate.of(2025, 6, 30))
                .build();

        ProjectNode node3 = ProjectNode.builder()
                .projectNodeId(3L)
                .projectId(projectId)
                .title("노드 3")
                .description("노드 3 설명")
                .nodeStatus(NodeStatus.PENDING_REVIEW)
                .priority(Priority.LOW)
                .nodeOrder(3)
                .contractStartDate(LocalDate.of(2025, 7, 1))
                .contractEndDate(LocalDate.of(2025, 9, 30))
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Arrays.asList(node1, node2, node3));

        // when
        List<NodeListResponse> result = readProjectNodeService.getNodeListByProject(projectId);

        // then
        assertThat(result).hasSize(3);

        // 첫 번째 노드 검증
        assertThat(result.get(0).projectNodeId()).isEqualTo(1L);
        assertThat(result.get(0).title()).isEqualTo("노드 1");
        assertThat(result.get(0).nodeOrder()).isEqualTo(1);
        assertThat(result.get(0).nodeStatus()).isEqualTo(NodeStatus.NOT_STARTED);
        assertThat(result.get(0).priority()).isEqualTo(Priority.HIGH);

        // 두 번째 노드 검증
        assertThat(result.get(1).projectNodeId()).isEqualTo(2L);
        assertThat(result.get(1).title()).isEqualTo("노드 2");
        assertThat(result.get(1).nodeOrder()).isEqualTo(2);
        assertThat(result.get(1).nodeStatus()).isEqualTo(NodeStatus.IN_PROGRESS);
        assertThat(result.get(1).priority()).isEqualTo(Priority.MEDIUM);

        // 세 번째 노드 검증
        assertThat(result.get(2).projectNodeId()).isEqualTo(3L);
        assertThat(result.get(2).title()).isEqualTo("노드 3");
        assertThat(result.get(2).nodeOrder()).isEqualTo(3);
        assertThat(result.get(2).nodeStatus()).isEqualTo(NodeStatus.PENDING_REVIEW);
        assertThat(result.get(2).priority()).isEqualTo(Priority.LOW);

        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
    }

    @Test
    @DisplayName("노드 리스트 조회 시 ProjectNodeService를 정확히 한 번만 호출한다")
    void givenProjectId_whenGetNodeList_thenCallProjectNodeServiceOnce() {
        // given
        Long projectId = 100L;
        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Collections.emptyList());

        // when
        readProjectNodeService.getNodeListByProject(projectId);

        // then
        verify(projectNodeService, times(1)).findByProjectIdByNodeOrder(projectId);
        verifyNoMoreInteractions(projectNodeService);
    }

    @Test
    @DisplayName("계약 기간이 설정된 노드의 정보를 올바르게 반환한다")
    void givenNodeWithContractDates_whenGetNodeList_thenReturnWithContractDates() {
        // given
        Long projectId = 100L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        ProjectNode node = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(projectId)
                .title("노드 1")
                .description("노드 1 설명")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .priority(Priority.HIGH)
                .nodeOrder(1)
                .contractStartDate(startDate)
                .contractEndDate(endDate)
                .build();

        when(projectNodeService.findByProjectIdByNodeOrder(projectId))
                .thenReturn(Collections.singletonList(node));

        // when
        List<NodeListResponse> result = readProjectNodeService.getNodeListByProject(projectId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).starDate()).isEqualTo(startDate);
        assertThat(result.get(0).endDate()).isEqualTo(endDate);
        verify(projectNodeService).findByProjectIdByNodeOrder(projectId);
    }
}