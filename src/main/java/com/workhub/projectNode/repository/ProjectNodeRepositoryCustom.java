package com.workhub.projectNode.repository;

import java.util.List;
import java.util.Map;

public interface ProjectNodeRepositoryCustom {
    Map<Long, Long> countMapByProjectIdIn(List<Long> projectIds);
}
