package com.workhub.post.entity;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
                .build();
    }
}
