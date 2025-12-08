package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.*;
import com.workhub.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientMemberRepository clientMemberRepository;
    private final DevMemberRepository devMemberRepository;

    public Project saveProject(Project project){
        return projectRepository.save(project);
    }

    public Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    public List<ProjectClientMember> saveProjectClientMember(List<ProjectClientMember> projectClientMembers){
        return clientMemberRepository.saveAll(projectClientMembers);
    }

    public List<ProjectDevMember> saveProjectDevMember(List<ProjectDevMember> projectDevMembers){
        return devMemberRepository.saveAll(projectDevMembers);
    }

    public Project validateCompletedProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.COMPLETED.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);
        }
        return project;
    }

    public Project validateProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.IN_PROGRESS.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
        }
        return project;
    }
}
