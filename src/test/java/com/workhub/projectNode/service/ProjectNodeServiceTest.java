package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class ProjectNodeServiceTest {

    @Mock
    ProjectNodeRepository projectNodeRepository;

    @InjectMocks
    ProjectNodeService projectNodeService;

    private ProjectNode testNode;

    @BeforeEach
    void setUp() {
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

    @Test
    @DisplayName("프로젝트 노드 저장 성공")
    void saveProjectNode_Success() {
        // given
        given(projectNodeRepository.save(any(ProjectNode.class))).willReturn(testNode);

        // when
        ProjectNode result = projectNodeService.saveProjectNode(testNode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 노드");
    }

    @Test
    @DisplayName("프로젝트 ID로 노드 리스트 조회 성공")
    void findByProjectIdByNodeOrder_Success() {
        // given
        List<ProjectNode> nodeList = List.of(testNode);
        given(projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(anyLong()))
                .willReturn(nodeList);

        // when
        List<ProjectNode> result = projectNodeService.findByProjectIdByNodeOrder(100L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("노드 ID로 조회 성공")
    void findById_Success() {
        // given
        given(projectNodeRepository.findById(anyLong())).willReturn(Optional.of(testNode));

        // when
        ProjectNode result = projectNodeService.findById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("노드 ID로 조회 실패 - 노드 없음")
    void findById_NotFound() {
        // given
        given(projectNodeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectNodeService.findById(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NODE_NOT_FOUND);
    }

    @Test
    @DisplayName("노드 ID와 프로젝트 ID로 조회 성공")
    void findByIdAndProjectId_Success() {
        // given
        given(projectNodeRepository.findByProjectNodeIdAndProjectId(anyLong(), anyLong()))
                .willReturn(Optional.of(testNode));

        // when
        ProjectNode result = projectNodeService.findByIdAndProjectId(1L, 100L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProjectNodeId()).isEqualTo(1L);
        assertThat(result.getProjectId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("최대 노드 순서 조회 - 노드가 있는 경우")
    void findMaxNodeOrderByProjectId_HasNodes() {
        // given
        given(projectNodeRepository.findTopByProjectIdAndDeletedAtIsNullOrderByNodeOrderDesc(anyLong()))
                .willReturn(Optional.of(testNode));

        // when
        Integer result = projectNodeService.findMaxNodeOrderByProjectId(100L);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("최대 노드 순서 조회 - 노드가 없는 경우")
    void findMaxNodeOrderByProjectId_NoNodes() {
        // given
        given(projectNodeRepository.findTopByProjectIdAndDeletedAtIsNullOrderByNodeOrderDesc(anyLong()))
                .willReturn(Optional.empty());

        // when
        Integer result = projectNodeService.findMaxNodeOrderByProjectId(100L);

        // then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("노드와 프로젝트 일치 검증 성공")
    void validateNodeToProject_Success() {
        // given
        given(projectNodeRepository.findById(anyLong())).willReturn(Optional.of(testNode));

        // when & then
        projectNodeService.validateNodeToProject(1L, 100L);
    }

    @Test
    @DisplayName("노드와 프로젝트 일치 검증 실패 - 프로젝트 불일치")
    void validateNodeToProject_NotMatched() {
        // given
        given(projectNodeRepository.findById(anyLong())).willReturn(Optional.of(testNode));

        // when & then
        assertThatThrownBy(() -> projectNodeService.validateNodeToProject(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_PROJECT_POST);
    }
}
