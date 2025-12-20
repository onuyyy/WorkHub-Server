package com.workhub.history.repository;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.entity.UnifiedHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 통합 히스토리 커스텀 Repository
 * QueryDSL을 사용한 복잡한 조회 쿼리 정의
 */
public interface UnifiedHistoryRepositoryCustom {

    /**
     * 전체 히스토리를 페이징 조회 (관리자용)
     *
     * @param pageable 페이징 정보 (페이지 번호, 사이즈, 정렬)
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findAllHistory(Pageable pageable);

    /**
     * 히스토리 타입별 필터링 조회
     *
     * @param historyType 히스토리 타입 (POST, PROJECT 등)
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findByHistoryType(HistoryType historyType, Pageable pageable);

    /**
     * 히스토리 타입 배열 필터링 조회
     *
     * @param historyTypes 히스토리 타입 목록
     * @param pageable     페이징 정보
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findByHistoryTypes(java.util.List<HistoryType> historyTypes, Pageable pageable);

    /**
     * 액션 타입별 필터링 조회
     *
     * @param actionType 액션 타입 (CREATE, UPDATE, DELETE 등)
     * @param pageable   페이징 정보
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findByActionType(ActionType actionType, Pageable pageable);

    /**
     * 특정 사용자가 수정한 히스토리 조회
     *
     * @param updatedBy 수정자 ID
     * @param pageable  페이징 정보
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findByUpdatedBy(Long updatedBy, Pageable pageable);

    /**
     * 타겟 ID와 히스토리 타입으로 조회
     *
     * @param targetId    타겟 ID
     * @param historyType 히스토리 타입
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록
     */
    Page<UnifiedHistory> findByTargetIdAndHistoryType(Long targetId, HistoryType historyType, Pageable pageable);
}
