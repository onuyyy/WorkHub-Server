package com.workhub.projectNode.service;

import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class DeleteProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @Mock
    ProjectNodeValidator projectNodeValidator;

    @Mock
    HistoryRecorder historyRecorder;

    @InjectMocks
    DeleteProjectNodeService deleteProjectNodeService;

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
    @DisplayName("노드 삭제 성공 - soft delete")
    void deleteProjectNode_Success() {
        // given
        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByIdAndProjectId(anyLong(), anyLong())).willReturn(testNode);

        // when
        deleteProjectNodeService.deleteProjectNode(100L, 1L);

        // then
        assertThat(testNode.getDeletedAt()).isNotNull(); // soft delete 확인
        verify(projectNodeValidator).validateLoginUserPermission(100L, 1L);
        verify(projectNodeService).findByIdAndProjectId(1L, 100L);
    }

    @Test
    @DisplayName("노드 삭제 시 deletedAt 설정됨")
    void deleteProjectNode_DeletedAtIsSet() {
        // given
        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByIdAndProjectId(anyLong(), anyLong())).willReturn(testNode);

        LocalDateTime beforeDelete = LocalDateTime.now();

        // when
        deleteProjectNodeService.deleteProjectNode(100L, 1L);

        // then
        assertThat(testNode.getDeletedAt()).isNotNull();
        assertThat(testNode.getDeletedAt()).isAfterOrEqualTo(beforeDelete);
    }

    @Test
    @DisplayName("노드 삭제 시 권한 체크 수행")
    void deleteProjectNode_ValidatePermission() {
        // given
        willDoNothing().given(projectNodeValidator).validateLoginUserPermission(anyLong(), anyLong());
        given(projectNodeService.findByIdAndProjectId(anyLong(), anyLong())).willReturn(testNode);

        // when
        deleteProjectNodeService.deleteProjectNode(100L, 1L);

        // then
        verify(projectNodeValidator).validateLoginUserPermission(100L, 1L);
    }
}
