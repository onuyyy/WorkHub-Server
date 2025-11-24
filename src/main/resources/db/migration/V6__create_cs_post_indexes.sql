-- cs_post indexes
-- 프로젝트별 삭제 안 된 CS 게시글 조회 (최신순)
CREATE INDEX idx_cs_post_project_deleted_created
    ON cs_post(project_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- 작성자별 삭제 안 된 CS 게시글 조회
CREATE INDEX idx_cs_post_user_deleted_created
    ON cs_post(user_id, created_at DESC)
    WHERE deleted_at IS NULL;

-- cs_qna indexes
-- CS 게시글별 Q&A 조회 (최신순)
CREATE INDEX idx_cs_qna_post_created
    ON cs_qna(cs_post_id, created_at);

-- 대댓글 조회 (부분 인덱스)
CREATE INDEX idx_cs_qna_parent_created
    ON cs_qna(parent_qna_id, created_at)
    WHERE parent_qna_id IS NOT NULL;

-- 최상위 질문만 조회
CREATE INDEX idx_cs_qna_post_root
    ON cs_qna(cs_post_id, created_at)
    WHERE parent_qna_id IS NULL;

-- 사용자별 작성 QnA 조회
CREATE INDEX idx_cs_qna_user
    ON cs_qna(user_id, created_at DESC);

-- cs_post_file indexes
-- CS 게시글 파일 조회 (순서대로)
CREATE INDEX idx_cs_post_file_post_order
    ON cs_post_file(cs_post_id, file_order);