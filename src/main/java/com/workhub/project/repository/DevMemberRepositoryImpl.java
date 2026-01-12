package com.workhub.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.project.entity.ProjectDevMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.workhub.project.entity.QProjectDevMember.projectDevMember;

@Repository
@RequiredArgsConstructor
public class DevMemberRepositoryImpl implements DevMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProjectDevMember> findByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(projectDevMember)
                .where(projectDevMember.projectId.in(projectIds))
                .fetch();
    }
}
