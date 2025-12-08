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
@Table(name = "project_dev_member_history")
public class ProjectDevMemberHistory extends BaseHistoryEntity {

    public static ProjectDevMemberHistory of(Long targetId, ActionType actionType, String beforeData,
                                             Long originalCreator) {
        return ProjectDevMemberHistory.builder()
                .targetId(targetId)
                .actionType(actionType)
                .beforeData(beforeData)  // todo : 프로젝트 멤버 엔티티에 텍스트가 없는데, 이곳에 어떤 내용을 담아야 할 지 정해야 할 거 같습니다.
                .createdBy(originalCreator)
                .updatedBy(SecurityUtil.getCurrentUserIdOrThrow())
                .updatedAt(LocalDateTime.now())
                .ipAddress(SecurityUtil.getRemoteAddr().orElse(null))
                .userAgent(SecurityUtil.getUserAgent().orElse(null))
                .build();
    }
}
