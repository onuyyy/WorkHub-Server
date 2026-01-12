package com.workhub.global.repository;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseHistoryRepository<T extends BaseHistoryEntity> extends JpaRepository<T,Long> {

    Optional<T> findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
            Long targetId, ActionType actionType
    );
}
