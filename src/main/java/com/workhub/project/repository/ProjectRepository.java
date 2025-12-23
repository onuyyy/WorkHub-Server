package com.workhub.project.repository;

import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long>, ProjectRepositoryCustom {
    Long countByStatusIn(List<Status> statuses);
    Long countByStatus(Status status);
}
