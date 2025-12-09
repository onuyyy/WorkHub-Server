package com.workhub.project.repository;

import com.workhub.project.entity.Project;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> findByProjectIdIn(List<Long> projectIds);
}
