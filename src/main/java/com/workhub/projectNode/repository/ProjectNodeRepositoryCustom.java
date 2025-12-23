package com.workhub.projectNode.repository;

import com.workhub.project.entity.Status;
import com.workhub.projectNode.dto.ProjectNodeCategoryCount;
import com.workhub.projectNode.dto.ProjectNodeCount;
import com.workhub.projectNode.entity.NodeCategory;

import java.util.List;
import java.util.Map;

public interface ProjectNodeRepositoryCustom {
    Map<Long, Long> countMapByProjectIdIn(List<Long> projectIds);

    Map<Long, ProjectNodeCount> countTotalAndApprovedByProjectIdIn(List<Long> projectIds);

    Map<NodeCategory, ProjectNodeCategoryCount> countCategoryStatsByProjectStatus(Status projectStatus);
}
