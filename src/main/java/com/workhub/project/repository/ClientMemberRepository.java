package com.workhub.project.repository;

import com.workhub.project.entity.ProjectClientMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientMemberRepository extends JpaRepository<ProjectClientMember,Long> {

}
