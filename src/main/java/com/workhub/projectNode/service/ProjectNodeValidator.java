package com.workhub.projectNode.service;

import com.workhub.global.util.SecurityUtil;
import com.workhub.project.entity.Project;
import com.workhub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectNodeValidator {

    private final ProjectService projectService;

    public void validateLoginUserPermission(Long projectId, Long loginUserId) {
        if(!SecurityUtil.hasRole("ADMIN")){
            projectService.validateDevMemberForProject(projectId, loginUserId);
        }
    }

    public void validateProjectMemberPermission(Long projectId, Long userId) {
        if(!SecurityUtil.hasRole("ADMIN")){
            projectService.validateProjectMember(projectId, userId);
        }
    }

    public Project validateProjectAndDevMember(Long projectId, Long devMemberId){
        Project project = projectService.findProjectById(projectId);
        projectService.validateDevMemberForProject(projectId, devMemberId);

        return project;
    }
}
