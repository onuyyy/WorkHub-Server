-- ============================================
-- Rollback: Unified History View 삭제
-- ============================================
-- unified_history_view를 삭제하는 롤백 스크립트
-- 작성일: 2024-12-18

DROP VIEW IF EXISTS unified_history_view;

-- 실행 확인
SELECT 'unified_history_view dropped successfully' as status;
