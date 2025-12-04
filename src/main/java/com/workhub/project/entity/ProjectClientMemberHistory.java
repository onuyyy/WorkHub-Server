package com.workhub.project.entity;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "project_client_member_history")
public class ProjectClientMemberHistory extends BaseHistoryEntity {

    public static ProjectClientMemberHistory of(Long targetId) {
        return ProjectClientMemberHistory.builder()
                .targetId(targetId)
                .actionType(ActionType.CREATE)
                .beforeData("프로젝트 최초 투입")  // todo : 프로젝트 멤버 엔티티에 텍스트가 없는데, 이곳에 어떤 내용을 담아야 할 지 정해야 할 거 같습니다.
                .createdBy(SecurityUtil.getCurrentUserIdOrThrow())
                .updatedBy(SecurityUtil.getCurrentUserIdOrThrow())
                .ipAddress(SecurityUtil.getRemoteAddr().orElse(null))
                .userAgent(SecurityUtil.getUserAgent().orElse(null))
                .build();
    }

}
