package com.workhub.checklist.repository;

import com.workhub.checklist.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckListRepository extends JpaRepository<CheckList, Long>, CheckListRepositoryCustom {
    Optional<CheckList> findByProjectNodeId(Long nodeId);
}
