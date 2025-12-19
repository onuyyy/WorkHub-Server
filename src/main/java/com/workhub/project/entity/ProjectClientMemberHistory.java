package com.workhub.project.entity;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "project_client_member_history")
public class ProjectClientMemberHistory extends BaseHistoryEntity {

    public static ProjectClientMemberHistory of(Long targetId, ActionType actionType, String beforeData,
                                                Long originalCreator) {
        return ProjectClientMemberHistory.builder()
                .targetId(targetId)
                .actionType(actionType)
                .beforeData(beforeData)
                .createdBy(originalCreator)
                .updatedBy(SecurityUtil.getCurrentUserIdOrThrow())
                .updatedAt(LocalDateTime.now())
                .ipAddress(SecurityUtil.getRemoteAddr().orElse(null))
                .userAgent(SecurityUtil.getUserAgent().orElse(null))
                .build();
    }

}
