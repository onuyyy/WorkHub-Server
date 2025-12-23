package com.workhub.projectNode.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.dto.ConfirmStatusResponse;
import com.workhub.projectNode.dto.ProjectNodeCount;
import com.workhub.projectNode.entity.ConfirmStatus;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public ProjectNode findByIdAndProjectId(Long projectNodeId, Long projectId) {
        return projectNodeRepository.findByProjectNodeIdAndProjectId(projectNodeId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));
    }

    public Map<Long, Long> getProjectNodeCountMapByProjectIdIn(List<Long> projectIds) {
        return projectNodeRepository.countMapByProjectIdIn(projectIds);
    }

    public Map<Long, ProjectNodeCount> getProjectNodeTotalAndApprovedCountMapByProjectIdIn(List<Long> projectIds) {
        return projectNodeRepository.countTotalAndApprovedByProjectIdIn(projectIds);
    }

    public void validateNodeToProject(Long nodeId, Long projectId){
        ProjectNode node = findById(nodeId);
        if (!node.getProjectId().equals(projectId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_POST);
        }
    }

    public Integer findMaxNodeOrderByProjectId(Long projectId) {
        return projectNodeRepository.findTopByProjectIdAndDeletedAtIsNullOrderByNodeOrderDesc(projectId)
                .map(ProjectNode::getNodeOrder)
                .orElse(0);
    }

    public long countByProjectIdInAndStatusIn(List<Long> projectIds, List<NodeStatus> statuses) {
        return projectNodeRepository.countByProjectIdInAndNodeStatusIn(projectIds, statuses);
    }

    public ConfirmStatusResponse getNodeConfirmStatus(Long nodeId) {

        ConfirmStatusResponse statusResponse = projectNodeRepository.findConfirmStatusById(nodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

        ConfirmStatus finalStatus = Optional.ofNullable(statusResponse.confirmStatus())
                .orElse(ConfirmStatus.NOT_PENDING);
        String rejectText = Optional.ofNullable(statusResponse.rejectText())
                .orElse("");
        String nodeTitle = statusResponse.nodeTitle();

        return  ConfirmStatusResponse.from(finalStatus, rejectText, nodeTitle);
    }
}
