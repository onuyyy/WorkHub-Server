--idx_change_log_target indexes
-- 대상별 변경 이력 조회 (가장 빈번)
CREATE INDEX idx_change_log_target
    ON change_log(target_type, target_id, changed_at DESC);

-- 사용자별 변경 이력
CREATE INDEX idx_change_log_user
    ON change_log(changed_by, changed_at DESC);

-- 시간순 전체 조회 (관리자용)
CREATE INDEX idx_change_log_time
    ON change_log(changed_at DESC);

-- 액션별 조회 (선택)
CREATE INDEX idx_change_log_action
    ON change_log(action_log, changed_at DESC);