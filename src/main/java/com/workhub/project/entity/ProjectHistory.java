package com.workhub.project.entity;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "project_history")
public class ProjectHistory extends BaseHistoryEntity {

    public static ProjectHistory of(Project project, Long loginUser, String userIp, String userAgent) {
        return ProjectHistory.builder()
                .targetId(project.getProjectId())
                .actionType(ActionType.CREATE)
                .beforeData(project.getProjectDescription())
                .createdBy(loginUser)
                .updatedBy(loginUser)
                .updatedAt(LocalDateTime.now())
                .ipAddress(userIp)
                .userAgent(userAgent)
                .build();
    }
}
