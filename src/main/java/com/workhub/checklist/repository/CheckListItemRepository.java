package com.workhub.checklist.repository;

import com.workhub.checklist.entity.checkList.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListItemRepository extends JpaRepository<CheckListItem, Long> {

    List<CheckListItem> findAllByCheckListIdOrderByItemOrderAsc(Long checkListId);
}
