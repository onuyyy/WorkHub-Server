package com.workhub.dashboard.service;

import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.service.ProjectNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashBoardService {

    private final ProjectService projectService;
    private final ProjectNodeService projectNodeService;

    public DashBoardResponse getSummary(Long userId) {
        Set<Long> projectIds = new HashSet<>();
        projectService.getDevMemberByUserId(userId)
                .forEach(dev -> projectIds.add(dev.getProjectId()));
        projectService.getClientMemberByUserId(userId)
                .forEach(client -> projectIds.add(client.getProjectId()));

        if (projectIds.isEmpty()) {
            return new DashBoardResponse(0, 0);
        }

        long pending = projectNodeService.countByProjectIdInAndStatusIn(
                projectIds.stream().toList(),
                List.of(NodeStatus.PENDING_REVIEW)
        );

        return new DashBoardResponse(pending, projectIds.size());
    }
}
