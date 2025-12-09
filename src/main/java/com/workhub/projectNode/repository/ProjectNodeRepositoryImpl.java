package com.workhub.projectNode.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.workhub.projectNode.entity.QProjectNode.projectNode;

@Repository
@RequiredArgsConstructor
public class ProjectNodeRepositoryImpl implements ProjectNodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> countMapByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<com.querydsl.core.Tuple> results = queryFactory
                .select(projectNode.projectId, projectNode.count())
                .from(projectNode)
                .where(
                        projectNode.projectId.in(projectIds),
                        projectNode.deletedAt.isNull()
                )
                .groupBy(projectNode.projectId)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(projectNode.projectId),
                        tuple -> tuple.get(1, Long.class)
                ));
    }
}
