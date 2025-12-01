package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.project.dto.UpdateStatusRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectHistory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProjectStatusService {

    private final ProjectService projectService;

    public void updateProjectStatus(Long projectId,
                                    UpdateStatusRequest statusRequest,
                                    String userIp, String userAgent, Long userId) {

        Project project = updateStatus(projectId, statusRequest);
        updateProjectHistory(projectId, project, userIp, userAgent, userId);

    }

    private Project updateStatus(Long projectId, UpdateStatusRequest statusRequest) {

        Project project = projectService.findProjectById(projectId);
        project.updateProjectStatus(statusRequest.status());

        return project;
    }

    private void updateProjectHistory(Long projectId, Project project,
                                      String userIp, String userAgent, Long userId) {

        Long originalCreator = projectService.getProjectOriginalCreator(projectId);

        projectService.updateProjectHistory(ProjectHistory.of(project,
                ActionType.UPDATE, originalCreator,
                userId, userIp, userAgent));
    }
}
