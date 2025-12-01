package com.workhub.project.repository;

import com.workhub.project.entity.ProjectClientMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientMemberHistoryRepository extends JpaRepository<ProjectClientMemberHistory, Long> {
}
