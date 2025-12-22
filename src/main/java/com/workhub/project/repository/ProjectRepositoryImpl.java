package com.workhub.project.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.project.dto.request.ProjectListRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.workhub.project.entity.QProject.project;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Project> findProjectsWithPaging(
            List<Long> projectIds,
            LocalDate startDate,
            LocalDate endDate,
            Status status,
            ProjectListRequest.SortOrder sortOrder,
            Long cursor,
            int size
    ) {
        return queryFactory
                .selectFrom(project)
                .where(
                        projectIdIn(projectIds),
                        contractStartDateBetween(startDate, endDate),
                        statusEq(status),
                        cursorCondition(cursor, sortOrder),
                        project.status.ne(Status.DELETED)  // DELETED 상태 제외
                )
                .orderBy(getOrderSpecifier(sortOrder))
                .limit(size + 1)  // hasNext 확인을 위해 +1
                .fetch();
    }

    @Override
    public Long countProjectsOverlapping(LocalDate monthStart, LocalDate monthEnd) {
        if (monthStart == null || monthEnd == null) {
            return 0L;
        }

        Long count = queryFactory
                .select(project.count())
                .from(project)
                .where(
                        project.contractStartDate.loe(monthEnd),
                        project.contractEndDate.goe(monthStart),
                        project.deletedAt.isNull()
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * 프로젝트 ID 리스트 조건
     */
    private BooleanExpression projectIdIn(List<Long> projectIds) {
        return (projectIds == null || projectIds.isEmpty()) ? null : project.projectId.in(projectIds);
    }

    /**
     * 계약 시작일 범위 조건
     */
    private BooleanExpression contractStartDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return project.contractStartDate.between(startDate, endDate);
    }

    /**
     * 프로젝트 상태 조건
     */
    private BooleanExpression statusEq(Status status) {
        return status == null ? null : project.status.eq(status);
    }

    /**
     * 커서 기반 페이징 조건
     * 최신순: projectId < cursor (내림차순이므로 더 작은 ID)
     * 오래된순: projectId > cursor (오름차순이므로 더 큰 ID)
     */
    private BooleanExpression cursorCondition(Long cursor, ProjectListRequest.SortOrder sortOrder) {
        if (cursor == null) {
            return null;
        }

        return sortOrder == ProjectListRequest.SortOrder.LATEST
                ? project.projectId.lt(cursor)
                : project.projectId.gt(cursor);
    }

    /**
     * 정렬 조건 생성
     * 최신순: contractStartDate DESC, projectId DESC
     * 오래된순: contractStartDate ASC, projectId ASC
     */
    private OrderSpecifier<?>[] getOrderSpecifier(ProjectListRequest.SortOrder sortOrder) {
        return sortOrder == ProjectListRequest.SortOrder.LATEST
                ? new OrderSpecifier[]{project.contractStartDate.desc(), project.projectId.desc()}
                : new OrderSpecifier[]{project.contractStartDate.asc(), project.projectId.asc()};
    }
}
