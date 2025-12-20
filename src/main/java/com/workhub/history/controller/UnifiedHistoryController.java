package com.workhub.history.controller;

import com.workhub.global.entity.HistoryType;
import com.workhub.history.dto.UnifiedHistoryResponse;
import com.workhub.global.entity.ActionType;
import com.workhub.history.service.UnifiedHistoryService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 통합 히스토리 관리 Controller
 * - /api/v1/admin/histories: 관리자 전용 (모든 정보 포함)
 * - /api/v1/histories: 일반 사용자 (IP, userAgent 제외)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UnifiedHistoryController {

    private final UnifiedHistoryService unifiedHistoryService;

    // ========== 관리자 전용 API ==========

    /**
     * 전체 히스토리 조회 (관리자 전용)
     *
     * @param pageable 페이징 정보 (기본: 10개, updated_at 내림차순)
     * @return 페이징된 히스토리 목록
     */
    @GetMapping("/api/v1/admin/histories")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findAllHistory(
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Admin requested all history with pageable: {}", pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findAllHistory(pageable);

        return ApiResponse.success(histories, "전체 히스토리가 조회되었습니다.");
    }

    /**
     * 히스토리 타입별 조회 (관리자 전용)
     *
     * @param historyType 히스토리 타입 (POST, PROJECT, CHECK_LIST_ITEM 등)
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록
     */
    @GetMapping("/api/v1/admin/histories/type/{historyType}")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findByHistoryTypeForAdmin(
            @PathVariable HistoryType historyType,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Admin requested history by type: {} with pageable: {}", historyType, pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findByHistoryType(historyType, pageable);

        return ApiResponse.success(histories, historyType + " 타입의 히스토리가 조회되었습니다.");
    }

    /**
     * 액션 타입별 조회 (관리자 전용)
     *
     * @param actionType 액션 타입 (CREATE, UPDATE, DELETE, MOVE, HIDE)
     * @param pageable   페이징 정보
     * @return 페이징된 히스토리 목록
     */
    @GetMapping("/api/v1/admin/histories/action/{actionType}")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findByActionType(
            @PathVariable ActionType actionType,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Admin requested history by action type: {} with pageable: {}", actionType, pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findByActionType(actionType, pageable);

        return ApiResponse.success(histories, actionType + " 액션의 히스토리가 조회되었습니다.");
    }

    /**
     * 특정 사용자가 수정한 히스토리 조회 (관리자 전용)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 히스토리 목록
     */
    @GetMapping("/api/v1/admin/histories/user/{userId}")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Admin requested history by user: {} with pageable: {}", userId, pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findByUpdatedBy(userId, pageable);

        return ApiResponse.success(histories, "사용자 " + userId + "의 히스토리가 조회되었습니다.");
    }

    // ========== 일반 사용자 API (IP, userAgent 제외) ==========

    /**
     * 히스토리 타입별 조회 (일반 사용자용 - IP, userAgent 제외)
     *
     * @param historyType 히스토리 타입 (POST, PROJECT, CHECK_LIST_ITEM 등)
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록 (IP, userAgent 제외)
     */
    @GetMapping("/api/v1/histories/type/{historyType}")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findByHistoryTypeForPublic(
            @PathVariable HistoryType historyType,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Public user requested history by type: {} with pageable: {}", historyType, pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findByHistoryTypeForPublic(historyType, pageable);

        return ApiResponse.success(histories, historyType + " 타입의 히스토리가 조회되었습니다.");
    }

    /**
     * 특정 타겟의 히스토리를 조회 (일반 사용자용)
     *
     * @param targetId    히스토리 대상 ID
     * @param historyType 히스토리 타입
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록 (IP, userAgent 제외)
     */
    @GetMapping("/api/v1/histories/{targetId}")
    public ResponseEntity<ApiResponse<Page<UnifiedHistoryResponse>>> findByTargetId(
            @PathVariable Long targetId,
            @RequestParam("historyType") HistoryType historyType,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Public user requested history by targetId: {} and type: {} with pageable: {}", targetId, historyType, pageable);

        Page<UnifiedHistoryResponse> histories = unifiedHistoryService.findByTargetIdAndHistoryTypeForPublic(targetId, historyType, pageable);

        return ApiResponse.success(histories, "타겟 " + targetId + "의 히스토리가 조회되었습니다.");
    }
}
