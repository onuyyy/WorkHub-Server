package com.workhub.checklist.entity.comment;

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
@Table(name = "check_list_item_comment_history")
@Entity
public class CheckListItemCommentHistory extends BaseHistoryEntity {
    public static CheckListItemCommentHistory of(Long targetId, ActionType actionType, String beforeData,
                                                   Long originalCreator) {
        return CheckListItemCommentHistory.builder()
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
