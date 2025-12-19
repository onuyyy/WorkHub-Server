package com.workhub.history.repository;

import com.workhub.global.entity.UnifiedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 통합 히스토리 Repository
 * 기본 CRUD 기능 제공
 */
@Repository
public interface UnifiedHistoryRepository extends JpaRepository<UnifiedHistory, Long>, UnifiedHistoryRepositoryCustom {
}