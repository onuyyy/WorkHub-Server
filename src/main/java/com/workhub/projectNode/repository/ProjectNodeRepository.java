package com.workhub.projectNode.repository;

import com.workhub.projectNode.entity.ProjectNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectNodeRepository extends JpaRepository<ProjectNode,Long>, ProjectNodeRepositoryCustom {

    List<ProjectNode> findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(Long projectId);

    Optional<ProjectNode> findByProjectNodeIdAndProjectId(Long projectNodeId, Long projectId);

    List<ProjectNode> findByProjectIdAndDeletedAtIsNull(Long projectId);
}
