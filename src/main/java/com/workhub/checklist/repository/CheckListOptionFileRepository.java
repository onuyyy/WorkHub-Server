package com.workhub.checklist.repository;

import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListOptionFileRepository extends JpaRepository<CheckListOptionFile, Long> {

    List<CheckListOptionFile> findAllByCheckListOptionIdOrderByFileOrderAsc(Long checkListOptionId);
}
