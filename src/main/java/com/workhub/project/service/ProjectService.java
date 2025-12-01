package com.workhub.project.service;

import com.workhub.project.entity.*;
import com.workhub.project.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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
}
