package com.workhub.project.repository;

import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.project.entity.ProjectDevMemberHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface DevMemberHistoryRepository extends BaseHistoryRepository<ProjectDevMemberHistory> {
}
