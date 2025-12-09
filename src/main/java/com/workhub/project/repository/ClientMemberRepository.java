package com.workhub.project.repository;

import com.workhub.project.entity.ProjectClientMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientMemberRepository extends JpaRepository<ProjectClientMember,Long>, ClientMemberRepositoryCustom {

    List<ProjectClientMember> findByUserId(Long userId);
}
