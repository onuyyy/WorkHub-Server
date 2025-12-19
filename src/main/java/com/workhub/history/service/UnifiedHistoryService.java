package com.workhub.history.service;

import com.workhub.global.entity.HistoryType;
import com.workhub.history.dto.UnifiedHistoryResponse;
import com.workhub.history.dto.UserInfo;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.UnifiedHistory;
import com.workhub.history.repository.UnifiedHistoryRepository;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 통합 히스토리 서비스
 * 관리자가 전체 히스토리를 조회하는 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnifiedHistoryService {

    private final UnifiedHistoryRepository unifiedHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 전체 히스토리 조회 (관리자용)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 히스토리 목록
     */
    public Page<UnifiedHistoryResponse> findAllHistory(Pageable pageable) {
        log.debug("Fetching all history with pageable: {}", pageable);

        Page<UnifiedHistory> historyPage = unifiedHistoryRepository.findAllHistory(pageable);

        return convertToResponsePage(historyPage, pageable);
    }

    /**
     * 히스토리 타입별 조회 (관리자용 - 모든 정보 포함)
     *
     * @param historyType 히스토리 타입 (POST, PROJECT 등)
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록
     */
    public Page<UnifiedHistoryResponse> findByHistoryType(HistoryType historyType, Pageable pageable) {
        log.debug("Fetching history by type: {} with pageable: {}", historyType, pageable);

        Page<UnifiedHistory> historyPage = unifiedHistoryRepository.findByHistoryType(historyType, pageable);

        return convertToResponsePage(historyPage, pageable);
    }

    /**
     * 히스토리 타입별 조회 (일반 사용자용 - IP, userAgent 제외)
     *
     * @param historyType 히스토리 타입 (POST, PROJECT 등)
     * @param pageable    페이징 정보
     * @return 페이징된 히스토리 목록 (IP, userAgent 제외)
     */
    public Page<UnifiedHistoryResponse> findByHistoryTypeForPublic(HistoryType historyType, Pageable pageable) {
        log.debug("Fetching public history by type: {} with pageable: {}", historyType, pageable);

        Page<UnifiedHistory> historyPage = unifiedHistoryRepository.findByHistoryType(historyType, pageable);

        return convertToPublicResponsePage(historyPage, pageable);
    }

    /**
     * 액션 타입별 조회
     *
     * @param actionType 액션 타입 (CREATE, UPDATE, DELETE 등)
     * @param pageable   페이징 정보
     * @return 페이징된 히스토리 목록
     */
    public Page<UnifiedHistoryResponse> findByActionType(ActionType actionType, Pageable pageable) {
        log.debug("Fetching history by action type: {} with pageable: {}", actionType, pageable);

        Page<UnifiedHistory> historyPage = unifiedHistoryRepository.findByActionType(actionType, pageable);

        return convertToResponsePage(historyPage, pageable);
    }

    /**
     * 특정 사용자가 수정한 히스토리 조회
     *
     * @param updatedBy 수정자 ID
     * @param pageable  페이징 정보
     * @return 페이징된 히스토리 목록
     */
    public Page<UnifiedHistoryResponse> findByUpdatedBy(Long updatedBy, Pageable pageable) {
        log.debug("Fetching history by updatedBy: {} with pageable: {}", updatedBy, pageable);

        Page<UnifiedHistory> historyPage = unifiedHistoryRepository.findByUpdatedBy(updatedBy, pageable);

        return convertToResponsePage(historyPage, pageable);
    }

    /**
     * Page<UnifiedHistory>를 Page<UnifiedHistoryResponse>로 변환
     * 모든 사용자 정보를 한 번에 조회
     */
    private Page<UnifiedHistoryResponse> convertToResponsePage(Page<UnifiedHistory> historyPage, Pageable pageable) {
        List<UnifiedHistory> histories = historyPage.getContent();

        // 1. 모든 고유한 userId 수집 (createdBy, updatedBy)
        Set<Long> userIds = histories.stream()
                .flatMap(history -> Stream.of(history.getCreatedBy(), history.getUpdatedBy()))
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());

        // 2. 모든 사용자 정보를 한 번에 조회
        Map<Long, UserInfo> userInfoMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        UserTable::getUserId,
                        UserInfo::from
                ));

        // 3. 히스토리를 응답 DTO로 변환
        List<UnifiedHistoryResponse> responses = histories.stream()
                .map(history -> UnifiedHistoryResponse.from(
                        history,
                        userInfoMap.get(history.getCreatedBy()),
                        userInfoMap.get(history.getUpdatedBy())
                ))
                .toList();

        return new PageImpl<>(responses, pageable, historyPage.getTotalElements());
    }

    /**
     * Page<UnifiedHistory>를 일반 사용자용 Page<UnifiedHistoryResponse>로 변환
     * IP, userAgent 제외
     */
    private Page<UnifiedHistoryResponse> convertToPublicResponsePage(Page<UnifiedHistory> historyPage, Pageable pageable) {
        List<UnifiedHistory> histories = historyPage.getContent();

        // 1. 모든 고유한 userId 수집 (createdBy, updatedBy)
        Set<Long> userIds = histories.stream()
                .flatMap(history -> Stream.of(history.getCreatedBy(), history.getUpdatedBy()))
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());

        // 2. 모든 사용자 정보를 한 번에 조회
        Map<Long, UserInfo> userInfoMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        UserTable::getUserId,
                        UserInfo::from
                ));

        // 3. 히스토리를 일반 사용자용 응답 DTO로 변환 (IP, userAgent 제외)
        List<UnifiedHistoryResponse> responses = histories.stream()
                .map(history -> UnifiedHistoryResponse.from(
                        history,
                        userInfoMap.get(history.getCreatedBy()),
                        userInfoMap.get(history.getUpdatedBy())
                ).toPublicResponse())  // IP, userAgent 제외
                .toList();

        return new PageImpl<>(responses, pageable, historyPage.getTotalElements());
    }
}