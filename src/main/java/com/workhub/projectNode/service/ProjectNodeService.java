package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectNodeService {

    private final ProjectNodeRepository projectNodeRepository;

    public ProjectNode saveProjectNode(ProjectNode projectNode){
        return projectNodeRepository.save(projectNode);
    }

    public List<ProjectNode> findByProjectIdByNodeOrder(Long projectId) {
        return projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    public ProjectNode findById(Long projectNodeId){
        return projectNodeRepository.findById(projectNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));
    }

}
