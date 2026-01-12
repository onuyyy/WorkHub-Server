package com.workhub.checklist.repository;

import com.workhub.checklist.entity.checkList.CheckListTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListTemplateRepository extends JpaRepository<CheckListTemplate, Long> {

    List<CheckListTemplate> findAllByOrderByTemplateIdDesc();
}
