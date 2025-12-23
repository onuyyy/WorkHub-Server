package com.workhub.dashboard.service;

import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.entity.Project;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.service.ProjectNodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

    @Mock
    ProjectService projectService;
    @Mock
    ProjectNodeService projectNodeService;

    @InjectMocks
    DashBoardService dashBoardService;

    @Test
    @DisplayName("개발사 역할이면 PENDING 노드 수와 총 프로젝트를 반환한다")
    void getSummary_dev() {
        given(projectService.getDevMemberByUserId(1L)).willReturn(List.of(
                ProjectDevMember.builder().projectId(10L).build(),
                ProjectDevMember.builder().projectId(11L).build()
        ));
        given(projectService.getClientMemberByUserId(1L)).willReturn(List.of());
        given(projectService.findActiveProjectsByIds(anyList())).willReturn(List.of(
                Project.builder().projectId(10L).build(),
                Project.builder().projectId(11L).build()
        ));
        given(projectNodeService.countByProjectIdInAndStatusIn(anyList(), eq(List.of(NodeStatus.PENDING_REVIEW))))
                .willReturn(4L);

        DashBoardResponse res = dashBoardService.getSummary(1L);

        assertThat(res.pendingApprovals()).isEqualTo(4L);
        assertThat(res.totalProjects()).isEqualTo(2L);
    }

    @Test
    @DisplayName("고객사 역할이면 PENDING 노드 수와 총 프로젝트를 반환한다")
    void getSummary_client() {
        given(projectService.getDevMemberByUserId(2L)).willReturn(List.of());
        given(projectService.getClientMemberByUserId(2L)).willReturn(List.of(
                ProjectClientMember.builder().projectId(20L).build()
        ));
        given(projectService.findActiveProjectsByIds(anyList())).willReturn(List.of(
                Project.builder().projectId(20L).build()
        ));
        given(projectNodeService.countByProjectIdInAndStatusIn(anyList(), eq(List.of(NodeStatus.PENDING_REVIEW))))
                .willReturn(1L);

        DashBoardResponse res = dashBoardService.getSummary(2L);

        assertThat(res.pendingApprovals()).isEqualTo(1L);
        assertThat(res.totalProjects()).isEqualTo(1L);
    }

    @Test
    @DisplayName("소속 프로젝트가 없으면 0,0을 반환한다")
    void getSummary_empty() {
        given(projectService.getDevMemberByUserId(3L)).willReturn(List.of());
        given(projectService.getClientMemberByUserId(3L)).willReturn(List.of());

        DashBoardResponse res = dashBoardService.getSummary(3L);

        assertThat(res.pendingApprovals()).isZero();
        assertThat(res.totalProjects()).isZero();
    }
}
