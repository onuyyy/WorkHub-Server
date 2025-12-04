package com.workhub.projectNode.repository;

import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectNodeHistoryRepository extends BaseHistoryRepository<ProjectNodeHistory> {

    /*Optional<ProjectNodeHistory> findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
            Long projectNodeId, ActionType actionType);*/

}
