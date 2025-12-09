package com.workhub.project.repository;

import com.workhub.project.entity.ProjectDevMember;

import java.util.List;

public interface DevMemberRepositoryCustom {
    List<ProjectDevMember> findByProjectIdIn(List<Long> projectIds);
}
