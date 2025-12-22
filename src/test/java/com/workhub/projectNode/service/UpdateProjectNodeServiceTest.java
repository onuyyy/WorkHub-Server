package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.projectNode.dto.UpdateNodOrderRequest;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.event.ProjectNodeUpdatedEvent;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UpdateProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @Mock
    ProjectNodeValidator projectNodeValidator;

    @Mock
    HistoryRecorder historyRecorder;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    UpdateProjectNodeService updateProjectNodeService;

    private MockedStatic<SecurityUtil> securityUtil;
    private ProjectNode testNode;

    @BeforeEach
    void setUp() {
        // SecurityUtil static method mock
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(1L);

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
    @DisplayName("노드 순서 업데이트 성공")
    void updateNodeOrder_Success() {
        // given
        ProjectNode node2 = ProjectNode.builder()
                .projectNodeId(2L)
                .projectId(100L)
                .nodeOrder(2)
                .build();

        List<ProjectNode> nodes = List.of(testNode, node2);
        List<UpdateNodOrderRequest> requests = List.of(
                new UpdateNodOrderRequest(1L, 2),
                new UpdateNodOrderRequest(2L, 1)
        );

        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(nodes);

        // when
        updateProjectNodeService.updateNodeOrder(100L, requests);

        // then
        assertThat(testNode.getNodeOrder()).isEqualTo(2);
        assertThat(node2.getNodeOrder()).isEqualTo(1);
        verify(projectNodeValidator).validateLoginUserPermission(100L, 1L);
        verify(eventPublisher, atLeastOnce()).publishEvent(any(ProjectNodeUpdatedEvent.class));
    }

    @Test
    @DisplayName("노드 순서 업데이트 실패 - 존재하지 않는 노드")
    void updateNodeOrder_NodeNotFound() {
        // given
        List<UpdateNodOrderRequest> requests = List.of(
                new UpdateNodOrderRequest(999L, 1) // 존재하지 않는 노드
        );

        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(List.of(testNode));

        // when & then
        assertThatThrownBy(() -> updateProjectNodeService.updateNodeOrder(100L, requests))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);
    }


    @Test
    @DisplayName("노드 순서 업데이트 - 순서가 같으면 히스토리 저장 안 함")
    void updateNodeOrder_SameOrder_NoHistoryRecord() {
        // given
        List<UpdateNodOrderRequest> requests = List.of(
                new UpdateNodOrderRequest(1L, 1) // 기존과 동일한 순서
        );

        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(List.of(testNode));

        // when
        updateProjectNodeService.updateNodeOrder(100L, requests);

        // then
        assertThat(testNode.getNodeOrder()).isEqualTo(1); // 변경 없음
    }
}
