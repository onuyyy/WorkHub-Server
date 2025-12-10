package com.workhub.checklist.service;

import com.workhub.global.util.SecurityUtil;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNode.service.ProjectNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckListAccessValidator {

    private final ProjectNodeService projectNodeService;
    private final ProjectService projectService;

    public void validateProjectAndNode(Long projectId, Long nodeId) {
        projectNodeService.findByIdAndProjectId(nodeId, projectId);

        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        projectService.validateProjectMember(projectId, userId);
    }
}
