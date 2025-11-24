-- check_list indexes
-- 프로젝트 단계별 체크리스트 조회
CREATE INDEX idx_check_list_project_node
    ON check_list(project_node_id);

-- check_list_item indexes
-- 체크리스트별 아이템 조회 (순서대로)
CREATE INDEX idx_check_list_item_list_order
    ON check_list_item(check_list_id, item_order);

-- 담당자별 체크리스트 아이템 조회
CREATE INDEX idx_check_list_item_user
    ON check_list_item(user_id, confirmed_at DESC);

-- 템플릿별 아이템 조회
CREATE INDEX idx_check_list_item_template
    ON check_list_item(template_id);

-- 동의 여부별 필터링
CREATE INDEX idx_check_list_item_confirm
    ON check_list_item(check_list_id, confirm, item_order);

-- check_list_comment_file indexes
-- 체크리스트 댓글 파일 (순서대로)
CREATE INDEX idx_check_list_comment_file_comment_order
    ON check_list_comment_file(cl_comment_id, file_order);

-- check_list_comment indexes
-- 체크리스트 아이템별 댓글 조회 (최신순)
CREATE INDEX idx_check_list_comment_item_created
    ON check_list_comment(check_list_item_id, created_at DESC);

-- 대댓글 조회
CREATE INDEX idx_check_list_comment_parent_created
    ON check_list_comment(parent_cl_comment_id, created_at DESC)
    WHERE parent_cl_comment_id IS NOT NULL;

-- check_list_template indexes
-- 체크리스트 템플릿 - 해시태그별 조회
CREATE INDEX idx_check_list_template_hashtag
    ON check_list_template(hashtag);

-- check_list_item_file indexes
-- 체크리스트 아이템 첨부파일 (순서대로)
CREATE INDEX idx_check_list_item_file_item_order
    ON check_list_item_file(check_list_item_id, file_order);