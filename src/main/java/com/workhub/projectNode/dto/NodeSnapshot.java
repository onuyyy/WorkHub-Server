package com.workhub.projectNode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.Priority;
import com.workhub.projectNode.entity.ProjectNode;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record NodeSnapshot(

        Long projectId,
        Long projectNodeId,
        String title,
        String description,
        NodeStatus nodeStatus,
        Integer nodeOrder,
        Priority priority,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime updatedAt,
        LocalDate starDate,
        LocalDate endDate
        // todo : 추후 체크리스트, 게시판의 첨부파일과 링크 총 게수를 담아서 응답해야합니다.

) {
    public static NodeSnapshot from(ProjectNode projectNode) {
        return NodeSnapshot.builder()
                .projectId(projectNode.getProjectId())
                .projectNodeId(projectNode.getProjectNodeId())
                .title(projectNode.getTitle())
                .description(projectNode.getDescription())
                .nodeStatus(projectNode.getNodeStatus())
                .nodeOrder(projectNode.getNodeOrder())
                .priority(projectNode.getPriority())
                .updatedAt(projectNode.getUpdatedAt())
                .starDate(projectNode.getContractStartDate())
                .endDate(projectNode.getContractEndDate())
                .build();
    }
}
