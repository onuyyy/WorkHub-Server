package com.workhub.project.repository;

import com.workhub.project.entity.ProjectDevMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevMemberHistoryRepository extends JpaRepository<ProjectDevMemberHistory, Long> {
}
