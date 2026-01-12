package com.workhub.global.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * UnifiedHistory의 복합 키
 * changeLogId는 각 히스토리 테이블마다 독립적으로 증가하므로
 * historyType과 함께 복합 키로 사용해야 고유성 보장
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedHistoryId implements Serializable {

    private Long changeLogId;
    private HistoryType historyType;
}
