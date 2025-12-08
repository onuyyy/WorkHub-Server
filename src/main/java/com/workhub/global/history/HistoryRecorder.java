package com.workhub.global.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HistoryRecorder {

    private final Map<HistoryType, HistoryHandler> handlerMap;
    private final ObjectMapper objectMapper;

    public HistoryRecorder(List<HistoryHandler> handlers, ObjectMapper objectMapper) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(HistoryHandler::getType, h -> h));
        this.objectMapper = objectMapper;
    }

    /**
     * 통합 히스토리 저장 메서드
     */
    public void recordHistory(HistoryType type, Long targetId, ActionType actionType, String beforeData) {

        Long creator;

        if (actionType == ActionType.CREATE) {
            // CREATE 액션일 때는 현재 사용자가 생성자
            creator = SecurityUtil.getCurrentUserIdOrThrow();
        } else {
            // UPDATE, DELETE 등일 때는 원래 생성자를 조회
            creator = getOriginalCreator(type, targetId);
        }

        HistoryHandler handler = getHandler(type);

        BaseHistoryEntity history = handler.createHistory(
                targetId, actionType, beforeData, creator
        );
        handler.save(history);

        log.debug("History recorded: type={}, targetId={}, action={}",
                type, targetId, actionType);
    }

    /**
     * 스냅샷 객체를 받아서 JSON으로 변환 후 히스토리 저장
     *
     * @param type 히스토리 타입
     * @param targetId 대상 엔티티 ID
     * @param actionType 액션 타입
     * @param snapshot JSON으로 변환될 스냅샷 객체
     */
    public void recordHistory(HistoryType type, Long targetId, ActionType actionType, Object snapshot) {
        String beforeData = toJsonString(snapshot);
        recordHistory(type, targetId, actionType, beforeData);
    }

    /**
     * originalCreator 조회
     */
    public Long getOriginalCreator(HistoryType type, Long targetId) {
        HistoryHandler handler = getHandler(type);
        return handler.findOriginalCreator(targetId);
    }

    private HistoryHandler getHandler(HistoryType type) {
        HistoryHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for type: " + type);
        }
        return handler;
    }

    /**
     * 객체를 JSON 문자열로 변환
     * JsonProcessingException을 BusinessException으로 변환하여 처리
     *
     * @param object JSON으로 변환할 객체
     * @return JSON 문자열
     * @throws BusinessException JSON 직렬화 실패 시
     */
    private String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed for object: {}", object.getClass().getName(), e);
            throw new BusinessException(ErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }
}