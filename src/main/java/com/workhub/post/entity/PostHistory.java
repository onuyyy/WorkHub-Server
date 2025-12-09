package com.workhub.post.entity;

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
@Table(name = "post_history")
public class PostHistory extends BaseHistoryEntity {

    public static PostHistory of(Long targetId, ActionType actionType, String beforeData, Long creator) {
        return PostHistory.builder()
                .targetId(targetId)
                .actionType(actionType)
                .beforeData(beforeData)
                .createdBy(creator)
                .updatedBy(SecurityUtil.getCurrentUserIdOrThrow())
                .updatedAt(LocalDateTime.now())
                .ipAddress(SecurityUtil.getRemoteAddr().orElse(null))
                .userAgent(SecurityUtil.getUserAgent().orElse(null))
                .build();
    }
}
