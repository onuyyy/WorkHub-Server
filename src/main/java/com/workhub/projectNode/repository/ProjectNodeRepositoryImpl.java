package com.workhub.projectNode.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.project.entity.Status;
import com.workhub.projectNode.dto.ProjectNodeCategoryCount;
import com.workhub.projectNode.dto.ProjectNodeCount;
import com.workhub.projectNode.entity.NodeCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.workhub.project.entity.QProject.project;
import static com.workhub.projectNode.entity.QProjectNode.projectNode;
import static com.workhub.projectNode.entity.ConfirmStatus.APPROVED;
import static com.workhub.projectNode.entity.NodeStatus.DONE;

@Repository
@RequiredArgsConstructor
public class ProjectNodeRepositoryImpl implements ProjectNodeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> countMapByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
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

    @Override
    public Map<Long, ProjectNodeCount> countTotalAndApprovedByProjectIdIn(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
                .select(
                        projectNode.projectId,
                        projectNode.count(),
                        Expressions.numberTemplate(Long.class,
                                "sum(case when {0} = {1} then 1 else 0 end)",
                                projectNode.confirmStatus, APPROVED)
                )
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
                        tuple -> new ProjectNodeCount(
                                tuple.get(1, Long.class),
                                tuple.get(2, Long.class)
                        )
                ));
    }

    @Override
    public Map<NodeCategory, ProjectNodeCategoryCount> countCategoryStatsByProjectStatus(Status projectStatus) {
        if (projectStatus == null) {
            return Map.of();
        }

        NumberExpression<Long> totalCount = projectNode.projectNodeId.count();
        NumberExpression<Long> completedCount = Expressions.numberTemplate(Long.class,
                "sum(case when {0} = {1} then 1 else 0 end)",
                projectNode.nodeStatus, DONE);

        List<Tuple> results = queryFactory
                .select(
                        projectNode.nodeCategory,
                        totalCount,
                        completedCount
                )
                .from(projectNode)
                .join(project).on(project.projectId.eq(projectNode.projectId))
                .where(
                        project.status.eq(projectStatus),
                        project.deletedAt.isNull(),
                        projectNode.deletedAt.isNull()
                )
                .groupBy(projectNode.nodeCategory)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(projectNode.nodeCategory),
                        tuple -> new ProjectNodeCategoryCount(
                                tuple.get(projectNode.nodeCategory),
                                tuple.get(totalCount),
                                tuple.get(completedCount)
                        )
                ));
    }
}
