-- project_notification constraint
-- 정확히 하나의 FK만 NOT NULL이어야 함
ALTER TABLE project_notification
    ADD CONSTRAINT chk_notification_exactly_one_related CHECK (
        (CASE WHEN project_node_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN cs_qna_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN post_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN comment_id IS NOT NULL THEN 1 ELSE 0 END) = 1
        );

-- project_notification indexes
-- 사용자별 알림 조회 (최신순)
CREATE INDEX idx_notification_user_created
    ON project_notification(user_id, created_at DESC);

-- 읽지 않은 알림 조회
CREATE INDEX idx_notification_user_unread
    ON project_notification(user_id, read_at, created_at DESC);

-- FK별 단일 인덱스
CREATE INDEX idx_notification_project_node
    ON project_notification(project_node_id);

CREATE INDEX idx_notification_cs_qna
    ON project_notification(cs_qna_id);

CREATE INDEX idx_notification_post
    ON project_notification(post_id);

CREATE INDEX idx_notification_comment
    ON project_notification(comment_id);