package com.workhub.history.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.entity.QUnifiedHistory;
import com.workhub.global.entity.UnifiedHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UnifiedHistoryRepositoryImpl implements UnifiedHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UnifiedHistory> findAllHistory(Pageable pageable) {
        QUnifiedHistory history = QUnifiedHistory.unifiedHistory;

        // 1. 데이터 조회
        List<UnifiedHistory> content = queryFactory
                .selectFrom(history)
                .orderBy(history.updatedAt.desc(), history.changeLogId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회
        Long total = queryFactory
                .select(history.count())
                .from(history)
                .fetchOne();

        // 3. Page 객체 생성
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<UnifiedHistory> findByHistoryType(HistoryType historyType, Pageable pageable) {
        QUnifiedHistory history = QUnifiedHistory.unifiedHistory;

        // 1. 데이터 조회
        List<UnifiedHistory> content = queryFactory
                .selectFrom(history)
                .where(historyTypeEq(historyType))
                .orderBy(history.updatedAt.desc(), history.changeLogId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회
        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(historyTypeEq(historyType))
                .fetchOne();

        // 3. Page 객체 생성
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<UnifiedHistory> findByActionType(ActionType actionType, Pageable pageable) {
        QUnifiedHistory history = QUnifiedHistory.unifiedHistory;

        // 1. 데이터 조회
        List<UnifiedHistory> content = queryFactory
                .selectFrom(history)
                .where(actionTypeEq(actionType))
                .orderBy(history.updatedAt.desc(), history.changeLogId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회
        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(actionTypeEq(actionType))
                .fetchOne();

        // 3. Page 객체 생성
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<UnifiedHistory> findByUpdatedBy(Long updatedBy, Pageable pageable) {
        QUnifiedHistory history = QUnifiedHistory.unifiedHistory;

        // 1. 데이터 조회
        List<UnifiedHistory> content = queryFactory
                .selectFrom(history)
                .where(updatedByEq(updatedBy))
                .orderBy(history.updatedAt.desc(), history.changeLogId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회
        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(updatedByEq(updatedBy))
                .fetchOne();

        // 3. Page 객체 생성
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 히스토리 타입 조건
     */
    private BooleanExpression historyTypeEq(HistoryType historyType) {
        return historyType == null ? null : QUnifiedHistory.unifiedHistory.historyType.eq(historyType);
    }

    /**
     * 액션 타입 조건
     */
    private BooleanExpression actionTypeEq(ActionType actionType) {
        return actionType == null ? null : QUnifiedHistory.unifiedHistory.actionType.eq(actionType);
    }

    /**
     * 수정자 ID 조건
     */
    private BooleanExpression updatedByEq(Long updatedBy) {
        return updatedBy == null ? null : QUnifiedHistory.unifiedHistory.updatedBy.eq(updatedBy);
    }
}