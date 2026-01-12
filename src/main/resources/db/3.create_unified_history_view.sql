-- ============================================
-- Unified History View for Admin
-- ============================================
-- 모든 히스토리 테이블을 통합하여 관리자가 전체 히스토리를 조회할 수 있는 View
-- 작성일: 2024-12-18
-- 용도: 관리자 히스토리 페이지네이션 조회
--
-- 포함된 히스토리 테이블:
-- - post_history
-- - comment_history
-- - check_list_item_history
-- - check_list_item_comment_history
-- - cs_post_history
-- - cs_qna_history
-- - project_history
-- - project_node_history
-- - project_client_member_history
-- - project_dev_member_history
-- - user_history

-- 기존 View가 있다면 삭제
DROP VIEW IF EXISTS unified_history_view;

-- 통합 히스토리 View 생성
CREATE VIEW unified_history_view AS

-- POST 히스토리
SELECT
    'POST' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM post_history

UNION ALL

-- COMMENT 히스토리
SELECT
    'POST_COMMENT' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM comment_history

UNION ALL

-- CHECK_LIST_ITEM 히스토리
SELECT
    'CHECK_LIST_ITEM' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM check_list_item_history

UNION ALL

-- CHECK_LIST_ITEM_COMMENT 히스토리
SELECT
    'CHECK_LIST_ITEM_COMMENT' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM check_list_item_comment_history

UNION ALL

-- CS_POST 히스토리
SELECT
    'CS_POST' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM cs_post_history

UNION ALL

-- CS_QNA 히스토리
SELECT
    'CS_QNA' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM cs_qna_history

UNION ALL

-- PROJECT 히스토리
SELECT
    'PROJECT' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM project_history

UNION ALL

-- PROJECT_NODE 히스토리
SELECT
    'PROJECT_NODE' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM project_node_history

UNION ALL

-- PROJECT_CLIENT_MEMBER 히스토리
SELECT
    'PROJECT_CLIENT_MEMBER' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM project_client_member_history

UNION ALL

-- PROJECT_DEV_MEMBER 히스토리
SELECT
    'PROJECT_DEV_MEMBER' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM project_dev_member_history

UNION ALL

-- USER 히스토리
SELECT
    'USER' as history_type,
    change_log_id,
    target_id,
    action_type,
    before_data,
    created_by,
    updated_by,
    updated_at,
    ip_address,
    user_agent
FROM user_history;

-- 실행 확인
SELECT 'unified_history_view created successfully' as status;
