package com.workhub.global.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * 통합 히스토리 View 엔티티
 * 모든 히스토리 테이블을 UNION한 unified_history_view를 매핑
 * View이므로 수정 불가 (@Immutable)
 *
 * changeLogId는 각 히스토리 테이블마다 독립적으로 증가하므로
 * historyType과 함께 복합 키로 사용
 */
@Entity
@Table(name = "unified_history_view")
@IdClass(UnifiedHistoryId.class)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnifiedHistory {

    @Id
    @Column(name = "change_log_id")
    private Long changeLogId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "history_type", length = 50)
    private HistoryType historyType;

    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}