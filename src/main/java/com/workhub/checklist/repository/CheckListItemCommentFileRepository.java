package com.workhub.checklist.repository;

import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListItemCommentFileRepository extends JpaRepository<CheckListItemCommentFile, Long> {

    List<CheckListItemCommentFile> findAllByClCommentIdOrderByFileOrderAsc(Long clCommentId);
}