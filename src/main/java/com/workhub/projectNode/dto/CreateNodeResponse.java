package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateNodeResponse(
        Long projectNodeId,
        Long projectId,
        String title,
        String description,
        NodeStatus nodeStatus,
        LocalDate startDate,
        LocalDate endDate,
        Integer nodeOrder,
        Priority priority
) {
    public static CreateNodeResponse from(ProjectNode projectNode) {
        return CreateNodeResponse.builder()
                .projectNodeId(projectNode.getProjectNodeId())
                .projectId(projectNode.getProjectId())
                .title(projectNode.getTitle())
                .description(projectNode.getDescription())
                .nodeStatus(projectNode.getNodeStatus())
                .startDate(projectNode.getContractStartDate())
                .endDate(projectNode.getContractEndDate())
                .nodeOrder(projectNode.getNodeOrder())
                .priority(projectNode.getPriority())
                .build();
    }
}
