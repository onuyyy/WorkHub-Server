-- WorkHub Database Schema
-- PostgreSQL

-- ============================================
-- Company & User Tables
-- ============================================

CREATE TABLE company (
    company_id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(50) NOT NULL,
    company_number VARCHAR(20) NOT NULL,
    tel VARCHAR(20) NOT NULL,
    address VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE user_table (
    user_id BIGSERIAL PRIMARY KEY,
    login_id VARCHAR(30) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL,
    phone VARCHAR(12) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    lasted_at TIMESTAMP NULL,
    company_id BIGINT NULL
);

CREATE TABLE user_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

-- ============================================
-- Project Tables
-- ============================================

CREATE TABLE project (
    project_id BIGSERIAL PRIMARY KEY,
    project_title VARCHAR(50) NOT NULL,
    project_description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    contract_start_date DATE NULL,
    contract_end_date DATE NULL,
    client_company_id BIGINT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE project_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE project_dev_member (
    project_dev_member_id BIGSERIAL PRIMARY KEY,
    assigned_at DATE NULL,
    removed_at DATE NULL,
    dev_part VARCHAR(10) NOT NULL,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL
);

CREATE TABLE project_dev_member_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE project_client_member (
    project_client_member_id BIGSERIAL PRIMARY KEY,
    assigned_at DATE NULL,
    removed_at DATE NULL,
    role VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL
);

CREATE TABLE project_client_member_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

-- ============================================
-- Project Node Tables
-- ============================================

CREATE TABLE project_node (
    project_node_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    node_status VARCHAR(20) NOT NULL,
    confirm_status VARCHAR(20) NOT NULL,
    confirmed_at TIMESTAMP NULL,
    reject_text VARCHAR(255) NULL,
    node_order INTEGER NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE project_node_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

-- ============================================
-- Post Tables
-- ============================================

CREATE TABLE post (
    post_id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    hashtag VARCHAR(20) NULL,
    post_ip VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    project_node_id BIGINT NOT NULL,
    parent_post_id BIGINT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE post_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE post_comment (
    comment_id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_comment_id BIGINT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE comment_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE post_file (
    post_file_id BIGSERIAL PRIMARY KEY,
    file_url VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_order INTEGER NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE post_link (
    link_id BIGSERIAL PRIMARY KEY,
    reference_link VARCHAR(255) NOT NULL,
    link_description VARCHAR(255) NULL,
    post_id BIGINT NOT NULL
);

-- ============================================
-- CS (Customer Service) Tables
-- ============================================

CREATE TABLE cs_post (
    cs_post_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE cs_post_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE cs_post_file (
    cs_post_file_id BIGSERIAL PRIMARY KEY,
    file_url VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_order INTEGER NOT NULL,
    cs_post_id BIGINT NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE cs_qna (
    cs_qna_id BIGSERIAL PRIMARY KEY,
    qna_content TEXT NOT NULL,
    cs_post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_qna_id BIGINT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE cs_qna_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

-- ============================================
-- CheckList Tables
-- ============================================

CREATE TABLE check_list (
    check_list_id BIGSERIAL PRIMARY KEY,
    check_list_title VARCHAR(50) NOT NULL,
    project_node_id BIGINT NOT NULL
);

CREATE TABLE check_list_template (
    template_id BIGSERIAL PRIMARY KEY,
    item_title VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    template_hashtag VARCHAR(20) NULL
);

CREATE TABLE check_list_item (
    check_list_item_id BIGSERIAL PRIMARY KEY,
    item_title VARCHAR(50) NOT NULL,
    item_order INTEGER NOT NULL,
    comment TEXT NOT NULL,
    confirm BOOLEAN NULL,
    confirmed_at TIMESTAMP NULL,
    check_list_id BIGINT NOT NULL,
    template_id BIGINT NULL,
    user_id BIGINT NOT NULL
);

CREATE TABLE check_list_item_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE check_list_item_file (
    check_list_item_file_id BIGSERIAL PRIMARY KEY,
    file_url VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_order INTEGER NOT NULL,
    check_list_item_id BIGINT NOT NULL
);

CREATE TABLE check_list_item_comment (
    cl_comment_id BIGSERIAL PRIMARY KEY,
    cl_content TEXT NOT NULL,
    check_list_item_id BIGINT NOT NULL,
    parent_cl_comment_id BIGINT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE check_list_item_comment_history (
    change_log_id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    before_data TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    updated_at TIMESTAMP NULL,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255) NOT NULL
);

CREATE TABLE check_list_item_comment_file (
    comment_file_id BIGSERIAL PRIMARY KEY,
    file_url VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_order INTEGER NOT NULL,
    cl_comment_id BIGINT NOT NULL
);

-- ============================================
-- Notification Tables
-- ============================================

CREATE TABLE project_notification (
    project_notification_id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(50) NOT NULL,
    notification_content VARCHAR(100) NOT NULL,
    related_url VARCHAR(50) NULL,
    read_at TIMESTAMP NULL,
    user_id BIGINT NULL,
    project_node_id BIGINT NULL,
    cs_qna_id BIGINT NULL,
    post_id BIGINT NULL,
    comment_id BIGINT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

-- project_notification constraint
-- 정확히 하나의 FK만 NOT NULL이어야 함
ALTER TABLE project_notification
    ADD CONSTRAINT chk_notification_exactly_one_related CHECK (
        (CASE WHEN project_node_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN cs_qna_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN post_id IS NOT NULL THEN 1 ELSE 0 END +
         CASE WHEN comment_id IS NOT NULL THEN 1 ELSE 0 END) = 1
        );