package com.workhub.projectNode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workhub.project.dto.response.DevMembers;
import com.workhub.projectNode.entity.ConfirmStatus;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record NodeResponse(

        Long projectId,
        Long projectNodeId,
        String title,
        String description,
        NodeStatus nodeStatus,
        ConfirmStatus confirmStatus,
        Integer nodeOrder,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime updatedAt,
        LocalDate starDate,
        LocalDate endDate,
        DevMembers devMembers
        // todo : 추후 체크리스트, 게시판의 첨부파일과 링크 총 게수를 담아서 응답해야합니다.

) {

    public static NodeResponse from(ProjectNode projectNode, UserTable user) {
        return NodeResponse.builder()
                .projectId(projectNode.getProjectId())
                .projectNodeId(projectNode.getProjectNodeId())
                .title(projectNode.getTitle())
                .description(projectNode.getDescription())
                .nodeStatus(projectNode.getNodeStatus())
                .confirmStatus(projectNode.getConfirmStatus())
                .nodeOrder(projectNode.getNodeOrder())
                .devMembers(DevMembers.from(user))
                .updatedAt(projectNode.getUpdatedAt())
                .starDate(projectNode.getContractStartDate())
                .endDate(projectNode.getContractEndDate())
                .build();
    }
}
