package com.workhub.project.repository;

import com.workhub.project.entity.ProjectHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectHistoryRepository extends JpaRepository<ProjectHistory, Long> {
}
