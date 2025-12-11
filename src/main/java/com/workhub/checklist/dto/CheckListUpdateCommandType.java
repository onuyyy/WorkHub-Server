package com.workhub.checklist.dto;

/**
 * 체크리스트 업데이트 시 항목/선택지/파일에서 수행할 작업 구분
 */
public enum CheckListUpdateCommandType {
    CREATE,
    UPDATE,
    DELETE
}
