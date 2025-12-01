package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
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
    private final ProjectHistoryRepository projectHistoryRepository;
    private final ClientMemberRepository clientMemberRepository;
    private final DevMemberRepository devMemberRepository;
    private final ClientMemberHistoryRepository  clientMemberHistoryRepository;
    private final DevMemberHistoryRepository devMemberHistoryRepository;

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

    public void saveProjectHistory(ProjectHistory projectHistory){
        projectHistoryRepository.save(projectHistory);
    }

    public void saveProjectClientMemberHistory(List<ProjectClientMemberHistory> clientMemberHistories){
        clientMemberHistoryRepository.saveAll(clientMemberHistories);
    }

    public void saveProjectDevMemberHistory(List<ProjectDevMemberHistory> projectDevMemberHistories){
        devMemberHistoryRepository.saveAll(projectDevMemberHistories);
    }

    public Long getProjectOriginalCreator(Long projectId){

        return projectHistoryRepository
                .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(projectId, ActionType.CREATE)
                .map(ProjectHistory::getCreatedBy)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));

    }

    public void updateProjectHistory(ProjectHistory projectHistory){
        projectHistoryRepository.save(projectHistory);
    }
}
