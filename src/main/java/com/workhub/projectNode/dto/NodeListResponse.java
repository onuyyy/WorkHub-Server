package com.workhub.projectNode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.userTable.entity.UserTable;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record NodeListResponse(

        Long projectId,
        Long projectNodeId,
        String title,
        String description,
        NodeStatus nodeStatus,
        Integer nodeOrder,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime updatedAt,
        LocalDate starDate,
        LocalDate endDate,
        DevMembers devMembers
        // todo : 추후 체크리스트, 게시판의 첨부파일과 링크 총 게수를 담아서 응답해야합니다.

) {
    @Builder
    public static record DevMembers (
            Long devMemberId,
            String devMemberName
    ){
        public static DevMembers from(UserTable user) {
            return DevMembers.builder()
                    .devMemberId(user.getUserId())
                    .devMemberName(user.getUserName())
                    .build();
        }
    }

    public static NodeListResponse from(ProjectNode projectNode, UserTable user) {
        return NodeListResponse.builder()
                .projectId(projectNode.getProjectId())
                .projectNodeId(projectNode.getProjectNodeId())
                .title(projectNode.getTitle())
                .description(projectNode.getDescription())
                .nodeStatus(projectNode.getNodeStatus())
                .nodeOrder(projectNode.getNodeOrder())
                .devMembers(DevMembers.from(user))
                .updatedAt(projectNode.getUpdatedAt())
                .starDate(projectNode.getContractStartDate())
                .endDate(projectNode.getContractEndDate())
                .build();
    }
}
