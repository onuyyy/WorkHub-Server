package com.workhub.checklist.repository;

import com.workhub.checklist.entity.comment.CheckListItemComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListItemCommentRepository extends JpaRepository<CheckListItemComment, Long> {
}
