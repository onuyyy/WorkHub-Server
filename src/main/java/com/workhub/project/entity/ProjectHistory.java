package com.workhub.project.entity;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.util.SecurityUtil;
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

    public static ProjectHistory of(Long targetId, ActionType actionType, String beforeData,
                                    Long originalCreator) {

        return ProjectHistory.builder()
                .targetId(targetId)
                .actionType(actionType)
                .beforeData(beforeData)  // todo : 아떤 데이터가 들어가야 할 지 상의해봐야 합니다.
                .createdBy(originalCreator)
                .updatedBy(SecurityUtil.getCurrentUserIdOrThrow())
                .updatedAt(LocalDateTime.now())
                .ipAddress(SecurityUtil.getRemoteAddr().orElse(null))
                .userAgent(SecurityUtil.getUserAgent().orElse(null))
                .build();
    }
}
