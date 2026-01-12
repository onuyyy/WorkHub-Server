package com.workhub.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.project.entity.ProjectClientMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.workhub.project.entity.QProjectClientMember.projectClientMember;

@Repository
@RequiredArgsConstructor
public class ClientMemberRepositoryImpl implements ClientMemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProjectClientMember> findByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(projectClientMember)
                .where(projectClientMember.projectId.in(projectIds))
                .fetch();
    }
}
