package com.workhub.cs.repository.csQna;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.entity.QCsQna;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class CsQnaRepositoryImpl implements CsQnaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CsQna> findCsQnasWithReplies(Long csPostId, Pageable pageable) {
        QCsQna csQna = QCsQna.csQna;

        // 최상위 댓글만 페이징하여 조회
        List<CsQna> topLevelComments = queryFactory
                .selectFrom(csQna)
                .where(
                        csPostIdEq(csPostId),
                        csQna.parentQnaId.isNull()
                )
                .orderBy(csQna.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 최상위 댓글의 총 개수
        Long total = queryFactory
                .select(csQna.count())
                .from(csQna)
                .where(
                        csPostIdEq(csPostId),
                        csQna.parentQnaId.isNull()
                )
                .fetchOne();

        return new PageImpl<>(topLevelComments, pageable, total == null ? 0 : total);
    }

    @Override
    public List<CsQna> findAllByCsPostId(Long csPostId) {
        QCsQna csQna = QCsQna.csQna;

        return queryFactory
                .selectFrom(csQna)
                .where(csPostIdEq(csPostId))
                .orderBy(csQna.createdAt.asc())
                .fetch();
    }

    private BooleanExpression csPostIdEq(Long csPostId) {
        return csPostId == null ? null : QCsQna.csQna.csPostId.eq(csPostId);
    }
}