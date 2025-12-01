package com.workhub.project.repository;

import com.workhub.project.entity.ProjectDevMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevMemberRepository extends JpaRepository<ProjectDevMember,Long> {

}
