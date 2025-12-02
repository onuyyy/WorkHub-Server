package com.workhub.cs.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.cs.dto.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.cs.entity.QCsPost;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CsPostRepositoryImpl implements CsPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CsPost> findCsPosts(CsPostSearchRequest searchRequest, Pageable pageable) {

        QCsPost csPost = QCsPost.csPost;

        List<CsPost> result =  queryFactory
                .selectFrom(csPost)
                .where(
                        valueContains(searchRequest.searchValue()),
                        statusEq(searchRequest.csPostStatus())
                )
                .orderBy(csPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(csPost.count())
                .from(csPost)
                .where(
                        valueContains(searchRequest.searchValue()),
                        statusEq(searchRequest.csPostStatus())
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total == null ? 0 : total);
    }

    private BooleanExpression valueContains(String value) {
        return (value == null || value.isBlank())
                ? null
                : QCsPost.csPost.title.containsIgnoreCase(value)
                    .or(QCsPost.csPost.content.containsIgnoreCase(value));
    }

    private BooleanExpression statusEq(CsPostStatus status) {
        return status == null ? null : QCsPost.csPost.csPostStatus.eq(status);
    }
}
