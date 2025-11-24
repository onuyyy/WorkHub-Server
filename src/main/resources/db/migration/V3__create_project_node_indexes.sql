-- project_node indexes
-- 프로젝트별 활성 단계 조회 (가장 빈번)
CREATE INDEX idx_project_node_active
    ON project_node(project_id, node_order)
    WHERE deleted_at IS NULL;

-- 승인 대기 단계 조회
CREATE INDEX idx_project_node_pending
    ON project_node(project_id, confirm_status, node_order)
    WHERE deleted_at IS NULL AND confirm_status = 'PENDING';

-- 담당자별 단계 조회
CREATE INDEX idx_project_node_user
    ON project_node(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 유저가 담당한 프로젝트 노드 조회
CREATE INDEX idx_project_node_user_project_created
    ON project_node(user_id, project_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- project_node_history indexes
CREATE INDEX idx_project_node_history_node_status_updated
    ON project_node_history(project_node_id, status, updated_at);

CREATE INDEX idx_project_node_history_user_updated
    ON project_node_history(changed_by, updated_at);