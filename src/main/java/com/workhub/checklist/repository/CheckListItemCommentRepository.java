package com.workhub.checklist.repository;

import com.workhub.checklist.entity.comment.CheckListItemComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckListItemCommentRepository extends JpaRepository<CheckListItemComment, Long> {

    List<CheckListItemComment> findAllByCheckListItemIdAndDeletedAtIsNull(Long checkListItemId);

    @Query("SELECT c FROM CheckListItemComment c WHERE c.checkListItemId = :checkListItemId " +
            "AND c.parentClCommentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<CheckListItemComment> findTopLevelCommentsByCheckListItemId(@Param("checkListItemId") Long checkListItemId);

    List<CheckListItemComment> findAllByParentClCommentIdAndDeletedAtIsNull(Long parentClCommentId);
}
