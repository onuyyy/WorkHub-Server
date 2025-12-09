package com.workhub.checklist.repository;

import com.workhub.checklist.entity.CheckListItemHistory;
import com.workhub.global.repository.BaseHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListItemHistoryRepository extends BaseHistoryRepository<CheckListItemHistory> {
}
