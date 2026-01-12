package com.workhub.project.repository;

import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.project.entity.ProjectHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectHistoryRepository extends BaseHistoryRepository<ProjectHistory> {

    /*Optional<ProjectHistory> findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
            Long targetId, ActionType actionType);*/
}
