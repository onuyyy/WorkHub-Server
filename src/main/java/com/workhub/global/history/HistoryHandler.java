package com.workhub.global.history;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.repository.BaseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@RequiredArgsConstructor
public abstract class HistoryHandler {

    private final JpaRepository<? extends BaseHistoryEntity, Long> repository;
    private final HistoryCreator creator;

    public abstract HistoryType getType();

    public BaseHistoryEntity createHistory(Long targetId, ActionType actionType, String beforeData, Long creator) {

        return this.creator.create(targetId, actionType, beforeData, creator);
    }

    @SuppressWarnings("unchecked")
    public void save(BaseHistoryEntity history) {
        ((JpaRepository<BaseHistoryEntity, Long>) repository).save(history);
    }

    public Long findOriginalCreator(Long targetId) {

        if (repository instanceof BaseHistoryRepository) {

            return ((BaseHistoryRepository<?>) repository)
                    .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
                            targetId, ActionType.CREATE
                    )
                    .map(BaseHistoryEntity::getCreatedBy)
                    .orElse(null);
        }

        throw new IllegalStateException("Repository must extend BaseHistoryRepository");
    }
}