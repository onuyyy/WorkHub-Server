package com.workhub.cs.entity;

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
@Table(name = "cs_qna_history")
@Entity
public class CsQnaHistory extends BaseHistoryEntity {
    public static CsQnaHistory of(Long targetId, ActionType actionType, String beforeData,
                                   Long originalCreator) {

        return CsQnaHistory.builder()
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
