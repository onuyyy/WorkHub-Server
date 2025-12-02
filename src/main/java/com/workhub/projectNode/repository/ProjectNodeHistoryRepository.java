package com.workhub.projectNode.repository;

import com.workhub.projectNode.entity.ProjectNodeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectNodeHistoryRepository extends JpaRepository<ProjectNodeHistory,Long> {
}
