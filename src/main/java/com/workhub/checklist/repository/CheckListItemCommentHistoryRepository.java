package com.workhub.checklist.repository;

import com.workhub.checklist.entity.CheckListItemCommentHistory;
import com.workhub.global.repository.BaseHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListItemCommentHistoryRepository extends BaseHistoryRepository<CheckListItemCommentHistory> {
}
