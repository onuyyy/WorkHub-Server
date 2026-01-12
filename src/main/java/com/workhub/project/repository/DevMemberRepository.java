package com.workhub.project.repository;

import com.workhub.project.entity.ProjectDevMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevMemberRepository extends JpaRepository<ProjectDevMember,Long>, DevMemberRepositoryCustom {

    List<ProjectDevMember> findByUserId(Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long devMemberId);
}
