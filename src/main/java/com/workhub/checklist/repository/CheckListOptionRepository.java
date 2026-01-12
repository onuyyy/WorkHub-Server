package com.workhub.checklist.repository;

import com.workhub.checklist.entity.checkList.CheckListOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListOptionRepository extends JpaRepository<CheckListOption, Long> {

    List<CheckListOption> findAllByCheckListItemIdOrderByOptionOrderAsc(Long checkListItemId);
}
