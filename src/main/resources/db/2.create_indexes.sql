-- WorkHub Database Indexes
-- PostgreSQL

-- ============================================
-- Company & User Tables Indexes
-- ============================================

-- user_table indexes
-- 회사 기준 사용자 리스트 조회 + 상태, 역할 필터 + 최근순 (삭제되지 않은 사용자만)
CREATE INDEX idx_user_company_status_role_created
    ON user_table(company_id, status, created_at DESC)
    WHERE deleted_at IS NULL;

-- 역할 기준 사용자 리스트 조회 (삭제되지 않은 사용자만)
CREATE INDEX idx_user_role_status_created
    ON user_table(user_role, status, created_at DESC)
    WHERE deleted_at IS NULL;

-- 이메일로 사용자 조회 (로그인, 중복 체크 - 삭제되지 않은 사용자만)
CREATE INDEX idx_user_email
    ON user_table(email)
    WHERE deleted_at IS NULL;

-- login_id로 사용자 조회 (로그인 - 삭제되지 않은 사용자만)
CREATE INDEX idx_user_login_id
    ON user_table(login_id)
    WHERE deleted_at IS NULL;

-- 삭제된 사용자 조회
CREATE INDEX idx_user_deleted
    ON user_table(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- company indexes
-- 사업자명 기준 조회
CREATE INDEX idx_company_name
    ON company(company_name);

-- user_history indexes
-- 대상별 변경 이력 조회 (최신순)
CREATE INDEX idx_user_history_target_action_updated
    ON user_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_user_history_updated_by
    ON user_history(updated_by, updated_at DESC);

-- ============================================
-- Project Tables Indexes
-- ============================================

-- project indexes
-- 고객사별 프로젝트 조회 (최신순)
CREATE INDEX idx_project_client_company_created
    ON project(client_company_id, created_at DESC);

-- 상태별 프로젝트 조회
CREATE INDEX idx_project_status_created
    ON project(status, created_at DESC);

-- 전체 프로젝트 최신순 조회
CREATE INDEX idx_project_created
    ON project(created_at DESC);

-- project_history indexes
-- 프로젝트별 변경 이력 조회 (최신순)
CREATE INDEX idx_project_history_target_action_updated
    ON project_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_project_history_updated_by
    ON project_history(updated_by, updated_at DESC);

-- project_dev_member indexes
-- user_id와 project_id를 복합 UNIQUE로 설정
ALTER TABLE project_dev_member
    ADD CONSTRAINT uk_project_dev_member_user_project
        UNIQUE (user_id, project_id);

-- 프로젝트별 참여자 조회 (개발 파트별, 배정일순)
CREATE INDEX idx_project_dev_member_project_dev_assigned
    ON project_dev_member(project_id, dev_part, assigned_at DESC)
    WHERE removed_at IS NULL;

-- 유저별 참여 프로젝트 조회
CREATE INDEX idx_project_dev_member_user_assigned
    ON project_dev_member(user_id, assigned_at DESC)
    WHERE removed_at IS NULL;

-- project_dev_member_history indexes
-- project_dev_member 변경 이력 조회 (최신순)
CREATE INDEX idx_project_dev_member_history_target_action_updated
    ON project_dev_member_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_project_dev_member_history_updated_by
    ON project_dev_member_history(updated_by, updated_at DESC);

-- project_client_member indexes
-- 프로젝트별 고객사 멤버 조회 (권한별, 배정일순)
CREATE INDEX idx_project_client_member_user_assigned
    ON project_client_member(user_id, assigned_at DESC)
    WHERE removed_at IS NULL;

-- user_id와 project_id 복합 UNIQUE
ALTER TABLE project_client_member
    ADD CONSTRAINT uk_project_client_member_user_project
        UNIQUE (user_id, project_id);

-- 유저별 참여 프로젝트 조회
CREATE INDEX idx_project_client_member_project_role
    ON project_client_member(project_id, role, assigned_at DESC)
    WHERE removed_at IS NULL;

-- project_client_member_history indexes
--  project_client_member 변경 이력 조회 (최신순)
CREATE INDEX idx_project_client_member_history_target_action_updated
    ON project_client_member_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_project_client_member_history_updated_by
    ON project_client_member_history(updated_by, updated_at DESC);

-- ============================================
-- Project Node Tables Indexes
-- ============================================

-- project_node indexes
-- 프로젝트별 활성 단계 조회 (가장 빈번)
CREATE INDEX idx_project_node_active
    ON project_node(project_id, node_order)
    WHERE deleted_at IS NULL;

-- 승인 대기 단계 조회
CREATE INDEX idx_project_node_pending
    ON project_node(project_id, confirm_status, node_order)
    WHERE deleted_at IS NULL AND confirm_status = 'PENDING';

-- 개발자가 담당한 프로젝트 노드 조회
CREATE INDEX idx_project_node_developer_project_created
    ON project_node(developer_user_id, project_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 확인자가 담당한 프로젝트 노드 조회
CREATE INDEX idx_project_node_confirm_user_project_created
    ON project_node(confirm_user_id, project_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 삭제된 프로젝트 노드 조회
CREATE INDEX idx_project_node_deleted
    ON project_node(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- project_node_history indexes
-- 프로젝트 노드별 변경 이력 조회 (최신순)
CREATE INDEX idx_project_node_history_target_action_updated
    ON project_node_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_project_node_history_updated_by
    ON project_node_history(updated_by, updated_at DESC);

-- ============================================
-- Post Tables Indexes
-- ============================================

-- post indexes
-- 작성자별 게시글 조회
CREATE INDEX idx_post_user
    ON post(user_id);

-- 프로젝트 단계별 게시글 조회
CREATE INDEX idx_post_project_node
    ON post(project_node_id);

-- 최신 게시글 조회 (생성일 기준)
CREATE INDEX idx_post_created
    ON post(created_at DESC);

-- 게시글 타입별 조회
CREATE INDEX idx_post_type
    ON post(type);

-- 특정 게시글의 하위 게시글 조회
CREATE INDEX idx_post_parent
    ON post(parent_post_id);

-- post_history indexes
-- 게시글별 변경 이력 조회 (최신순)
CREATE INDEX idx_post_history_target_action_updated
    ON post_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_post_history_updated_by
    ON post_history(updated_by, updated_at DESC);

-- post_comment indexes
-- 게시글별 댓글 최신순 조회
CREATE INDEX idx_post_comment_post_created
    ON post_comment(post_id, created_at DESC);

-- 대댓글 조회
CREATE INDEX idx_post_comment_parent
    ON post_comment(parent_comment_id);

-- 사용자별 작성 댓글 조회
CREATE INDEX idx_post_comment_user
    ON post_comment(user_id, created_at DESC);

-- comment_history indexes
-- 댓글별 변경 이력 조회 (최신순)
CREATE INDEX idx_comment_history_target_action_updated
    ON comment_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_comment_history_updated_by
    ON comment_history(updated_by, updated_at DESC);

-- post_file indexes
-- 게시글 파일 조회 (순서대로, 삭제되지 않은 파일만)
CREATE INDEX idx_post_file_storage_order
    ON post_file(post_id, file_order)
    WHERE deleted_at IS NULL;

-- 삭제된 파일 조회
CREATE INDEX idx_post_file_deleted
    ON post_file(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- post_link indexes
-- 게시글별 링크 조회
CREATE INDEX idx_post_link_post
    ON post_link(post_id);

-- ============================================
-- CS (Customer Service) Tables Indexes
-- ============================================

-- cs_post indexes
-- 프로젝트별 삭제 안 된 CS 게시글 조회 (최신순)
CREATE INDEX idx_cs_post_project_deleted_created
    ON cs_post(project_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 작성자별 삭제 안 된 CS 게시글 조회
CREATE INDEX idx_cs_post_user_deleted_created
    ON cs_post(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 삭제된 CS 게시글 조회
CREATE INDEX idx_cs_post_deleted
    ON cs_post(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- cs_post_history indexes
-- CS 게시글별 변경 이력 조회 (최신순)
CREATE INDEX idx_cs_post_history_target_action_updated
    ON cs_post_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_cs_post_history_updated_by
    ON cs_post_history(updated_by, updated_at DESC);

-- cs_post_file indexes
-- CS 게시글 파일 조회 (순서대로, 삭제되지 않은 파일만)
CREATE INDEX idx_cs_post_file_post_order
    ON cs_post_file(cs_post_id, file_order)
    WHERE deleted_at IS NULL;

-- 삭제된 CS 파일 조회
CREATE INDEX idx_cs_post_file_deleted
    ON cs_post_file(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- cs_qna indexes
-- CS 게시글별 Q&A 조회 (최신순, 삭제되지 않은 것만)
CREATE INDEX idx_cs_qna_post_created
    ON cs_qna(cs_post_id, created_at)
    WHERE deleted_at IS NULL;

-- 대댓글 조회 (삭제되지 않은 것만)
CREATE INDEX idx_cs_qna_parent_created
    ON cs_qna(parent_qna_id, created_at)
    WHERE parent_qna_id IS NOT NULL AND deleted_at IS NULL;

-- 최상위 질문만 조회 (삭제되지 않은 것만)
CREATE INDEX idx_cs_qna_post_root
    ON cs_qna(cs_post_id, created_at)
    WHERE parent_qna_id IS NULL AND deleted_at IS NULL;

-- 사용자별 작성 QnA 조회 (삭제되지 않은 것만)
CREATE INDEX idx_cs_qna_user
    ON cs_qna(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 삭제된 QnA 조회
CREATE INDEX idx_cs_qna_deleted
    ON cs_qna(deleted_at DESC)
    WHERE deleted_at IS NOT NULL;

-- cs_qna_history indexes
-- CS Q&A별 변경 이력 조회 (최신순)
CREATE INDEX idx_cs_qna_history_target_action_updated
    ON cs_qna_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_cs_qna_history_updated_by
    ON cs_qna_history(updated_by, updated_at DESC);

-- ============================================
-- CheckList Tables Indexes
-- ============================================

-- check_list indexes
-- 프로젝트 단계별 체크리스트 조회
CREATE INDEX idx_check_list_project_node
    ON check_list(project_node_id);

-- 사용자별 체크리스트 조회
CREATE INDEX idx_check_list_user
    ON check_list(user_id);

-- check_list_item indexes
-- 체크리스트별 아이템 조회 (순서대로)
CREATE INDEX idx_check_list_item_list_order
    ON check_list_item(check_list_id, item_order);

-- 템플릿별 아이템 조회
CREATE INDEX idx_check_list_item_template
    ON check_list_item(template_id);

-- check_list_item_history indexes
-- 체크리스트 아이템별 변경 이력 조회 (최신순)
CREATE INDEX idx_check_list_item_history_target_action_updated
    ON check_list_item_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_check_list_item_history_updated_by
    ON check_list_item_history(updated_by, updated_at DESC);

-- check_list_option indexes
-- 체크리스트 아이템별 옵션 조회 (순서대로, 삭제되지 않은 옵션만)
CREATE INDEX idx_check_list_option_item_order
    ON check_list_option(check_list_item_id, option_order)
    WHERE deleted_at IS NULL;

-- check_list_option_file indexes
-- 체크리스트 옵션별 파일 조회 (순서대로, 삭제되지 않은 파일만)
CREATE INDEX idx_check_list_option_file_option_order
    ON check_list_option_file(check_list_option_id, file_order)
    WHERE deleted_at IS NULL;

-- check_list_item_comment indexes
-- 체크리스트 아이템별 댓글 조회 (최신순, 삭제되지 않은 것만)
CREATE INDEX idx_check_list_item_comment_item_created
    ON check_list_item_comment(check_list_item_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 대댓글 조회 (삭제되지 않은 것만)
CREATE INDEX idx_check_list_item_comment_parent_created
    ON check_list_item_comment(parent_cl_comment_id, created_at DESC)
    WHERE parent_cl_comment_id IS NOT NULL AND deleted_at IS NULL;

-- 사용자별 작성 댓글 조회 (삭제되지 않은 것만)
CREATE INDEX idx_check_list_item_comment_user
    ON check_list_item_comment(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- check_list_item_comment_file indexes
-- 체크리스트 댓글별 파일 조회 (순서대로, 삭제되지 않은 파일만)
CREATE INDEX idx_check_list_item_comment_file_comment_order
    ON check_list_item_comment_file(cl_comment_id, file_order)
    WHERE deleted_at IS NULL;

-- check_list_item_comment_history indexes
-- 체크리스트 댓글별 변경 이력 조회 (최신순)
CREATE INDEX idx_check_list_item_comment_history_target_action_updated
    ON check_list_item_comment_history(target_id, action_type, updated_at DESC);

-- 변경자별 이력 조회
CREATE INDEX idx_check_list_item_comment_history_updated_by
    ON check_list_item_comment_history(updated_by, updated_at DESC);

-- ============================================
-- Notification Tables Indexes
-- ============================================

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