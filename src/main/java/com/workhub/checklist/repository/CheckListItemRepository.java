package com.workhub.checklist.repository;

import com.workhub.checklist.entity.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListItemRepository extends JpaRepository<CheckListItem, Long> {
}
