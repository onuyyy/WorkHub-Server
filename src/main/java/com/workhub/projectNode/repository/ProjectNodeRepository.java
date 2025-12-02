package com.workhub.projectNode.repository;

import com.workhub.projectNode.entity.ProjectNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectNodeRepository extends JpaRepository<ProjectNode,Long> {

    List<ProjectNode> findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(Long projectId);
}
