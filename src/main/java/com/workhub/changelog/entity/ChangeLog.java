package com.workhub.changelog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "change_log")
@Entity
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_log_id")
    private Long changeLogId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_log")
    private ActionLogType actionLog;
}