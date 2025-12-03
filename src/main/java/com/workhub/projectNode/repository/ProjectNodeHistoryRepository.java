package com.workhub.projectNode.repository;

import com.workhub.global.entity.ActionType;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectNodeHistoryRepository extends JpaRepository<ProjectNodeHistory,Long> {

    Optional<ProjectNodeHistory> findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
            Long projectNodeId, ActionType actionType);
}
