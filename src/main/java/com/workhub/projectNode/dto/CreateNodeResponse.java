package com.workhub.projectNode.dto;

import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import lombok.Builder;

@Builder
public record CreateNodeResponse(
        Long projectNodeId,
        Long projectId,
        String title,
        String description,
        NodeStatus nodeStatus,
        Integer nodeOrder,
        String priority
) {
    public static CreateNodeResponse from(ProjectNode projectNode) {
        return CreateNodeResponse.builder()
                .projectNodeId(projectNode.getProjectNodeId())
                .projectId(projectNode.getProjectId())
                .title(projectNode.getTitle())
                .description(projectNode.getDescription())
                .nodeStatus(projectNode.getNodeStatus())
                .nodeOrder(projectNode.getNodeOrder())
                .build();
    }
}
