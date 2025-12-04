package com.workhub.global.history;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.BaseHistoryEntity;
import com.workhub.global.entity.HistoryType;
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

    public HistoryRecorder(List<HistoryHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(HistoryHandler::getType, h -> h));
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
}