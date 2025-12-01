package com.workhub.project.repository;

import com.workhub.global.entity.ActionType;
import com.workhub.project.entity.ProjectHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, Long> {

    Optional<ProjectHistory> findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(
            Long targetId, ActionType actionType);
}
