package com.workhub.projectNode.repository;

import com.workhub.projectNode.dto.ConfirmStatusResponse;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectNodeRepository extends JpaRepository<ProjectNode,Long>, ProjectNodeRepositoryCustom {

    List<ProjectNode> findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(Long projectId);

    Optional<ProjectNode> findByProjectNodeIdAndProjectId(Long projectNodeId, Long projectId);

    List<ProjectNode> findByProjectIdAndDeletedAtIsNull(Long projectId);

    Optional<ProjectNode> findTopByProjectIdAndDeletedAtIsNullOrderByNodeOrderDesc(Long projectId);

    long countByProjectIdInAndNodeStatusIn(List<Long> projectIds, List<NodeStatus> statuses);

    @Query("SELECT p.confirmStatus, p.rejectText FROM ProjectNode p WHERE p.projectNodeId = :nodeId")
    Optional<ConfirmStatusResponse> findConfirmStatusById(@Param("nodeId") Long nodeId);
}
