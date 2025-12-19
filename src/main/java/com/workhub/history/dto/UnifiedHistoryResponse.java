package com.workhub.history.dto;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.entity.UnifiedHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UnifiedHistoryResponse {

    private Long changeLogId;
    private HistoryType historyType;
    private Long targetId;
    private ActionType actionType;
    private String beforeData;
    private UserInfo createdBy;
    private UserInfo updatedBy;
    private LocalDateTime updatedAt;
    private String ipAddress;
    private String userAgent;

    public static UnifiedHistoryResponse from(UnifiedHistory history, UserInfo createdBy, UserInfo updatedBy) {
        return UnifiedHistoryResponse.builder()
                .changeLogId(history.getChangeLogId())
                .historyType(history.getHistoryType())
                .targetId(history.getTargetId())
                .actionType(history.getActionType())
                .beforeData(history.getBeforeData())
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .updatedAt(history.getUpdatedAt())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .build();
    }

    /**
     * 일반 사용자용 응답으로 변환 (IP, userAgent 제외)
     */
    public UnifiedHistoryResponse toPublicResponse() {
        return UnifiedHistoryResponse.builder()
                .changeLogId(this.changeLogId)
                .historyType(this.historyType)
                .targetId(this.targetId)
                .actionType(this.actionType)
                .beforeData(this.beforeData)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .updatedAt(this.updatedAt)
                // ipAddress, userAgent 제외
                .build();
    }
}