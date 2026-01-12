package com.workhub.projectNode.service;

import com.workhub.global.util.SecurityUtil;
import com.workhub.projectNode.dto.NodeResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ReadProjectNodeServiceTest {

    @Mock
    ProjectNodeService projectNodeService;

    @Mock
    ProjectNodeValidator projectNodeValidator;

    @Mock
    UserService userService;

    @InjectMocks
    ReadProjectNodeService readProjectNodeService;

    private MockedStatic<SecurityUtil> securityUtil;
    private ProjectNode testNode1;
    private ProjectNode testNode2;
    private UserTable developer1;
    private UserTable developer2;

    @BeforeEach
    void setUp() {
        // SecurityUtil static method mock
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(1L);

        testNode1 = ProjectNode.builder()
                .projectNodeId(1L)
                .projectId(100L)
                .title("첫 번째 노드")
                .description("설명1")
                .nodeStatus(NodeStatus.NOT_STARTED)
                .nodeOrder(1)
                .developerUserId(10L)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusDays(30))
                .build();

        testNode2 = ProjectNode.builder()
                .projectNodeId(2L)
                .projectId(100L)
                .title("두 번째 노드")
                .description("설명2")
                .nodeStatus(NodeStatus.IN_PROGRESS)
                .nodeOrder(2)
                .developerUserId(20L)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusDays(60))
                .build();

        developer1 = UserTable.builder()
                .userId(10L)
                .loginId("dev1")
                .email("dev1@test.com")
                .build();

        developer2 = UserTable.builder()
                .userId(20L)
                .loginId("dev2")
                .email("dev2@test.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @Test
    @DisplayName("프로젝트 노드 리스트 조회 성공")
    void getNodeListByProject_Success() {
        // given
        List<ProjectNode> nodes = List.of(testNode1, testNode2);
        Map<Long, UserTable> userMap = Map.of(10L, developer1, 20L, developer2);

        willDoNothing().given(projectNodeValidator).validateProjectMemberPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(nodes);
        given(userService.getUserMapByUserIdIn(anyList())).willReturn(userMap);

        // when
        List<NodeResponse> result = readProjectNodeService.getNodeListByProject(100L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("첫 번째 노드");
        assertThat(result.get(0).nodeOrder()).isEqualTo(1);
        assertThat(result.get(1).title()).isEqualTo("두 번째 노드");
        assertThat(result.get(1).nodeOrder()).isEqualTo(2);

        verify(projectNodeValidator).validateProjectMemberPermission(100L, 1L);
        verify(projectNodeService).findByProjectIdByNodeOrder(100L);
        verify(userService).getUserMapByUserIdIn(anyList());
    }

    @Test
    @DisplayName("노드 리스트 조회 시 권한 체크 수행")
    void getNodeListByProject_ValidatePermission() {
        // given
        willDoNothing().given(projectNodeValidator).validateProjectMemberPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(List.of());
        given(userService.getUserMapByUserIdIn(anyList())).willReturn(Map.of());

        // when
        readProjectNodeService.getNodeListByProject(100L);

        // then
        verify(projectNodeValidator).validateProjectMemberPermission(100L, 1L);
    }

    @Test
    @DisplayName("노드가 없는 경우 빈 리스트 반환")
    void getNodeListByProject_EmptyList() {
        // given
        willDoNothing().given(projectNodeValidator).validateProjectMemberPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(List.of());
        given(userService.getUserMapByUserIdIn(anyList())).willReturn(Map.of());

        // when
        List<NodeResponse> result = readProjectNodeService.getNodeListByProject(100L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("노드 리스트는 nodeOrder 기준으로 정렬되어 반환")
    void getNodeListByProject_OrderedByNodeOrder() {
        // given
        List<ProjectNode> nodes = List.of(testNode1, testNode2);
        Map<Long, UserTable> userMap = Map.of(10L, developer1, 20L, developer2);

        willDoNothing().given(projectNodeValidator).validateProjectMemberPermission(anyLong(), anyLong());
        given(projectNodeService.findByProjectIdByNodeOrder(anyLong())).willReturn(nodes);
        given(userService.getUserMapByUserIdIn(anyList())).willReturn(userMap);

        // when
        List<NodeResponse> result = readProjectNodeService.getNodeListByProject(100L);

        // then
        assertThat(result.get(0).nodeOrder()).isLessThan(result.get(1).nodeOrder());
    }
}
