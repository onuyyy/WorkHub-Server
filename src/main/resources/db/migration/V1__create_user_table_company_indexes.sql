-- user_table indexes
-- 회사 기준 사용자 리스트 조회 + 상태, 역할 필터 + 최근순
CREATE INDEX idx_user_company_status_role_created
    ON user_table(company_id, status, role, created_at DESC);

-- 역할 기준 사용자 리스트 조회
CREATE INDEX idx_user_role_status_created
    ON user_table(role, status, created_at DESC);

-- 이메일로 사용자 조회 (로그인, 중복 체크)
CREATE INDEX idx_user_email
    ON user_table(email);

-- login_id로 사용자 조회 (로그인)
CREATE INDEX idx_user_login_id
    ON user_table(login_id);

-- company indexes
-- 사업자명 기준 조회
CREATE INDEX idx_company_name
    ON company(company_name);