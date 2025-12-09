package com.workhub.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.workhub.project.entity.QProject.project;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Project> findByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(project)
                .where(project.projectId.in(projectIds))
                .fetch();
    }
}
