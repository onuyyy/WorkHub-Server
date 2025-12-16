package com.workhub.projectNode.repository;

import com.workhub.projectNode.dto.ProjectNodeCount;

import java.util.List;
import java.util.Map;

public interface ProjectNodeRepositoryCustom {
    Map<Long, Long> countMapByProjectIdIn(List<Long> projectIds);

    Map<Long, ProjectNodeCount> countTotalAndApprovedByProjectIdIn(List<Long> projectIds);
}
