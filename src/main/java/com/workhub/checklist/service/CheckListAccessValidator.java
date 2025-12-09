package com.workhub.checklist.service;

import com.workhub.projectNode.service.ProjectNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckListAccessValidator {

    private final ProjectNodeService projectNodeService;

    public void validateProjectAndNode(Long projectId, Long nodeId) {
        projectNodeService.findByIdAndProjectId(nodeId, projectId);
    }
}
