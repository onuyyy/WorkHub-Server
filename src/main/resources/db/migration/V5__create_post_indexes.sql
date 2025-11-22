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

-- 프로젝트 단계 + 생성일 복합 인덱스
CREATE INDEX idx_post_project_node_created
    ON post(project_node_id, created_at DESC);

-- 게시글 타입별 조회
CREATE INDEX idx_post_type
    ON post(type);

-- 해시태그별 게시글 조회
CREATE INDEX idx_post_hashtag
    ON post(hashtag);

-- 특정 게시글의 하위 게시글 조회
CREATE INDEX idx_post_parent
    ON post(parent_post_id);

-- post_file indexes
-- 게시글 파일 조회 (순서대로)
CREATE INDEX idx_post_file_storage_order
    ON post_file(post_id, file_order);

-- link indexes
-- 게시글별 링크 조회
CREATE INDEX idx_link_post
    ON link(post_id);

-- comment indexes
-- 대댓글 조회
CREATE INDEX idx_comment_parent
    ON comment(parent_comment_id);

-- 댓글 생성일 조회
CREATE INDEX idx_comment_created
    ON comment(created_at DESC);

-- 게시글별 댓글 최신순 조회 (정렬 최적화)
CREATE INDEX idx_comment_post_created
    ON comment(post_id, created_at DESC);

-- 사용자별 작성 댓글 조회
CREATE INDEX idx_comment_user
    ON comment(user_id, created_at DESC);