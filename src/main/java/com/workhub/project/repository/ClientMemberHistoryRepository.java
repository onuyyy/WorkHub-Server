package com.workhub.project.repository;

import com.workhub.global.repository.BaseHistoryRepository;
import com.workhub.project.entity.ProjectClientMemberHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientMemberHistoryRepository extends BaseHistoryRepository<ProjectClientMemberHistory> {
}
