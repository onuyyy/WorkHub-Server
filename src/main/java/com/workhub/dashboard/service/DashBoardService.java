package com.workhub.dashboard.service;

import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.service.ProjectNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashBoardService {

    private final ProjectService projectService;
    private final ProjectNodeService projectNodeService;

    public DashBoardResponse getSummary(Long userId, String role) {
        List<Long> projectIds = isDev(role)
                ? projectService.getDevMemberByUserId(userId).stream().map(ProjectDevMember::getProjectId).toList()
                : projectService.getClientMemberByUserId(userId).stream().map(ProjectClientMember::getProjectId).toList();
        if (projectIds.isEmpty()) return new DashBoardResponse(0, 0);

        // 개발사/고객사 모두 승인 대기(PENDING) 노드 수를 카운트해 보여준다.
        long pendingOrApproved = projectNodeService.countByProjectIdInAndStatusIn(projectIds, List.of(NodeStatus.PENDING_REVIEW));

        return new DashBoardResponse(pendingOrApproved, projectIds.size());
    }

    private boolean isDev(String role) {
        return role != null && role.contains("DEV");
    }
}
