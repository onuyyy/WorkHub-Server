package com.workhub.global.history;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;

@FunctionalInterface
public interface HistoryCreator {
    BaseHistoryEntity create(Long targetId, ActionType actionType, String beforeData,
                             Long creator);
}