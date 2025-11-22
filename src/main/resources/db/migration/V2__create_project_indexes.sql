-- project indexes
-- 고객사별 프로젝트 조회 (최신순)
CREATE INDEX idx_project_client_company_created
    ON project(client_company_id, created_at DESC);

-- 상태별 프로젝트 조회
CREATE INDEX idx_project_status_created
    ON project(status, created_at DESC);

-- 계약 기간 검색 (특정 기간 내 계약)
CREATE INDEX idx_project_contract_dates
    ON project(contract_start_date, contract_end_date);

-- 전체 프로젝트 최신순 조회
CREATE INDEX idx_project_created
    ON project(created_at DESC);

-- project_dev_member
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

-- 개발 파트별 멤버 조회
CREATE INDEX idx_project_dev_member_dev_part_assigned
    ON project_dev_member(dev_part, assigned_at DESC)
    WHERE removed_at IS NULL;

-- project_client_member

-- 프로젝트별 고객사 멤버 조회 (권한별, 배정일순)
CREATE INDEX idx_project_client_member_user_assigned
    ON project_client_member(user_id, assigned_at DESC)
    WHERE removed_at IS NULL;

-- 유저별 참여 프로젝트 조회
ALTER TABLE project_client_member
    ADD CONSTRAINT uk_project_client_member_user_project
        UNIQUE (user_id, project_id);

-- user_id와 project_id 복합 UNIQUE
CREATE INDEX idx_project_client_member_project_role
    ON project_client_member(project_id, role, assigned_at DESC)
    WHERE removed_at IS NULL;