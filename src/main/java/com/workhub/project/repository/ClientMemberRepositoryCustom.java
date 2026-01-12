package com.workhub.project.repository;

import com.workhub.project.entity.ProjectClientMember;

import java.util.List;

public interface ClientMemberRepositoryCustom {
    List<ProjectClientMember> findByProjectIdIn(List<Long> projectIds);
}
