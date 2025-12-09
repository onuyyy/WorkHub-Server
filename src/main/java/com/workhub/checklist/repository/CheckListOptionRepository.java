package com.workhub.checklist.repository;

import com.workhub.checklist.entity.CheckListOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListOptionRepository extends JpaRepository<CheckListOption, Long> {
}