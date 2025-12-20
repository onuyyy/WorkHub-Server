package com.workhub.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // test
    TEST(HttpStatus.BAD_REQUEST, "001", "test error"),

    // server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "002", "서버에러, 에러가 계속될경우 담당자에게 문의 바랍니다."),

    // 공통 에러
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "E-001", "유효하지 않은 값입니다."),
    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "E-002", "잘못된 요청 형식입니다."),
    STATUS_ALREADY_SET(HttpStatus.BAD_REQUEST, "E-003", "이미 해당 상태입니다."),
    JSON_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E-004", "JSON 직렬화 처리 중 오류가 발생했습니다."),

    // 인증 && 인가 (세션 기반)
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "A-001", "세션이 만료되었습니다."),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED, "A-002", "유효하지 않은 세션입니다."),
    SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A-003", "세션이 존재하지 않습니다."),
    CSRF_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "A-004", "CSRF 토큰이 일치하지 않습니다."),
    INVALID_CSRF_TOKEN(HttpStatus.FORBIDDEN, "A-005", "유효하지 않은 CSRF 토큰입니다."),
    MAX_SESSION_EXCEEDED(HttpStatus.UNAUTHORIZED, "A-006", "최대 세션 수를 초과했습니다."),
    CONCURRENT_SESSION_LIMIT(HttpStatus.UNAUTHORIZED, "A-007", "동시 세션 제한에 도달했습니다."),
    FORBIDDEN_ADMIN(HttpStatus.FORBIDDEN, "A-008", "관리자 Role이 아닙니다."),
    NOT_EQUAL_PASSWORD(HttpStatus.UNAUTHORIZED, "A-009", "Password가 일치하지 않습니다."),
    NOT_EQUAL_CODE(HttpStatus.UNAUTHORIZED, "A-010", "Email Code가 일치하지 않습니다."),
    NOT_VALID_USER(HttpStatus.BAD_REQUEST, "A-011", "해당 회원에게 접근 권한이 없습니다."),
    NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "A-012", "로그인이 필요합니다."),
    INVALID_LOGIN_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A-013", "아이디 또는 비밀번호가 일치하지 않습니다."),

    // 회원
    INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "M-001", "잘못된 회원 타입입니다. (memberType : KAKAO)"),
    ALREADY_REGISTERED_USER(HttpStatus.BAD_REQUEST, "M-002", "이미 가입된 회원입니다."),
    USER_NOT_EXISTS(HttpStatus.BAD_REQUEST, "M-003", "해당 회원은 존재하지 않습니다."),
    NOT_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "M-004", "일치하는 Email 정보가 존재하지 않습니다."),
    ALREADY_EXISTS__EMAIL(HttpStatus.BAD_REQUEST, "M-005", "이미 존재하는 Email 입니다."),
    NOT_EXISTS_USER(HttpStatus.BAD_REQUEST, "M-006", "회원정보가 일치하지 않습니다."),
    USERNAME_IS_NULL(HttpStatus.BAD_REQUEST, "M-007", "username이 null 입니다."),
    NOT_ADMIN_USER(HttpStatus.BAD_REQUEST, "M-008", "Admin User가 아닙니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, "M-009", "탈퇴한 회원입니다."),
    DORMANT_USER(HttpStatus.BAD_REQUEST, "M-010", "휴면 계정입니다."),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "M-011", "잘못된 회원 역할입니다."),
    USER_MISMATCH(HttpStatus.FORBIDDEN, "M-012", "본인의 정보만 수정할 수 있습니다."),

    // 고객사
    COMPANY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "CO-001", "이미 등록된 고객사입니다."),
    Company_NOT_EXISTS(HttpStatus.BAD_REQUEST, "CO-002", "존재하지 않는 고객사입니다."),

    // 게시물
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PO-001", "게시물을 찾을 수 없습니다."),
    PARENT_POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "PO-002", "부모 게시글을 찾을 수 없습니다."),
    ALREADY_DELETED_POST(HttpStatus.BAD_REQUEST, "PO-003", "이미 삭제된 게시글입니다."),
    ALREADY_DELETED_POST_COMMENT(HttpStatus.BAD_REQUEST, "PO-004", "이미 삭제된 댓글입니다."),
    FORBIDDEN_POST_UPDATE(HttpStatus.FORBIDDEN, "PO-004", "게시글 수정 권한이 없습니다."),
    FORBIDDEN_POST_DELETE(HttpStatus.FORBIDDEN, "PO-005", "게시글 삭제 권한이 없습니다."),
    NOT_MATCHED_PROJECT_POST(HttpStatus.BAD_REQUEST, "PO-006", "잘못된 프로젝트 또는 단계의 게시글입니다."),
    INVALID_PROJECT_STATUS_FOR_POST(HttpStatus.BAD_REQUEST, "PO-007", "진행중인 프로젝트에서만 게시글을 처리할 수 있습니다."),
    INVALID_POST_FILE_ORDER(HttpStatus.BAD_REQUEST, "PO-008", "파일 순서(fileOrder)는 0 이상의 값이어야 합니다."),
    NOT_EXISTS_POST_FILE(HttpStatus.BAD_REQUEST, "PO-009", "존재하지 않는 게시글 파일입니다."),
    INVALID_POST_FILE_UPDATE(HttpStatus.BAD_REQUEST, "PO-010", "잘못된 게시글 파일 수정 요청입니다."),
    NOT_EXISTS_POST_LINK(HttpStatus.BAD_REQUEST, "PO-011", "존재하지 않는 게시글 링크입니다."),
    INVALID_POST_LINK_UPDATE(HttpStatus.BAD_REQUEST, "PO-012", "잘못된 게시글 링크 수정 요청입니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "PO-013", "댓글 내용이 비어있습니다."),
    NOT_EXISTS_COMMENT(HttpStatus.BAD_REQUEST, "PO-014", "존재하지 않는 댓글입니다."),
    NOT_MATCHED_COMMENT_POST(HttpStatus.BAD_REQUEST, "PO-015", "잘못된 게시글의 댓글입니다."),
    INVALID_POST_FILE_CREATE(HttpStatus.BAD_REQUEST, "PO-010", "파일 URL이 비어있습니다."),



    // CS 게시판
    NOT_EXISTS_CS_POST(HttpStatus.BAD_REQUEST, "C-001", "존재하지 않는 CS 게시물입니다."),
    NOT_MATCHED_PROJECT_CS_POST(HttpStatus.BAD_REQUEST, "C-002", "잘못된 프로젝트의 게시글입니다."),
    INVALID_CS_POST_TITLE(HttpStatus.BAD_REQUEST, "C-003", "Title이 없습니다."),
    INVALID_CS_POST_CONTENT(HttpStatus.BAD_REQUEST, "C-004", "Content가 없습니다."),
    NOT_EXISTS_CS_POST_FILE(HttpStatus.BAD_REQUEST, "C-005", "존재하지 않는 CS 파일입니다."),
    INVALID_CS_POST_FILE_UPDATE(HttpStatus.BAD_REQUEST, "C-006", "잘못된 파일 업데이트 요청입니다."),
    INVALID_CS_POST_FILE_ORDER(HttpStatus.BAD_REQUEST, "C-007", "파일 순서(fileOrder)는 0 이상의 값이어야 합니다."),
    INVALID_FILE_UPDATE(HttpStatus.BAD_REQUEST, "C-008", "잘못된 파일 수정 요청입니다."),
    FORBIDDEN_CS_POST_UPDATE(HttpStatus.FORBIDDEN, "C-009", "CS 게시글 수정 권한이 없습니다."),
    FORBIDDEN_CS_POST_DELETE(HttpStatus.FORBIDDEN, "C-018", "CS 게시글 삭제 권한이 없습니다."),
    ALREADY_DELETED_CS_POST(HttpStatus.BAD_REQUEST, "C-0010", "이미 삭재된 CS 게시글입니다."),
    INVALID_PROJECT_STATUS_FOR_CS_POST(HttpStatus.BAD_REQUEST, "C-0011", "완료된 프로젝트에서만 CS 게시글을 처리할 수 있습니다."),
    NOT_EXISTS_CS_QNA(HttpStatus.BAD_REQUEST, "C-012", "존재하지 않는 CS 댓글입니다."),
    NOT_MATCHED_CS_QNA_POST(HttpStatus.BAD_REQUEST, "C-013", "잘못된 게시글의 CS 댓글입니다."),
    INVALID_CS_QNA_CONTENT(HttpStatus.BAD_REQUEST, "C-014", "CS 댓글 내용이 비어있습니다."),
    NOT_MATCHED_CS_QNA_USERID(HttpStatus.BAD_REQUEST, "C-015", "CS 댓글 작성자와 수정자가 다릅니다."),
    ALREADY_DELETED_CS_QNA(HttpStatus.BAD_REQUEST, "C-016", "이미 삭제된 CS 댓글입니다."),
    INVALID_CS_POST_FILE_CREATE(HttpStatus.BAD_REQUEST, "C-017", "CS 파일 URL이 비어있습니다."),

    // AWS S3
    // 파일 관련 에러
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-001", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-002", "파일 삭제에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F-003", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F-004", "지원하지 않는 파일 형식입니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "F-005", "파일 이름이 누락되었습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F-006", "파일 크기가 제한을 초과했습니다."),
    FILE_ACCESS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-007", "파일 접근에 실패했습니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "F-008", "올바르지 않은 파일 URL입니다."),

    // 프로젝트
    PROJECT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-001", "프로젝트 저장에 실패했습니다."),
    PROJECT_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-002", "프로젝트 히스토리 저장에 실패했습니다."),
    CLIENT_MEMBER_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-003", "고객사 멤버 저장에 실패했습니다."),
    DEV_MEMBER_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-004", "개발사 멤버 저장에 실패했습니다."),
    CLIENT_MEMBER_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-005", "고객사 멤버 히스토리 저장에 실패했습니다."),
    DEV_MEMBER_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR-006", "개발사 멤버 히스토리 저장에 실패했습니다."),
    PROJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "PR-007", "프로젝트를 찾을 수 없습니다."),
    PROJECT_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "PR-008", "프로젝트 히스토리를 찾을 수 없습니다."),
    NOT_EXISTS_DEV_MEMBER(HttpStatus.BAD_REQUEST, "PR-009", "해당 프로젝트를 담당하는 개발자가 아닙니다."),
    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "PR-010", "해당 프로젝트의 멤버가 아닙니다."),

    // 프로젝트 노드
    PROJECT_NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "PN-001", "프로젝트 노드를 찾을 수 없습니다."),
    PROJECT_NODE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PN-002", "프로젝트 노드 저장에 실패했습니다."),
    PROJECT_NODE_HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PN-003", "프로젝트 노드 히스토리 저장에 실패했습니다."),

    //알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NF-001", "알림을 찾을 수 없습니다."),

    // 체크리스트
    NOT_EXISTS_CHECK_LIST(HttpStatus.BAD_REQUEST, "CH-001", "존재하지 않는 체크리스트입니다."),
    ALREADY_EXISTS_CHECK_LIST(HttpStatus.BAD_REQUEST, "CH-002", "이미 체크리스트가 존재합니다."),
    INVALID_CHECK_LIST_ITEM_ORDER(HttpStatus.BAD_REQUEST, "CH-003", "항목 순서가 중복될 수 없습니다."),
    INVALID_CHECK_LIST_OPTION_ORDER(HttpStatus.BAD_REQUEST, "CH-004", "선택지 순서가 중복될 수 없습니다."),
    CHECK_LIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH-005", "체크리스트 항목을 찾을 수 없습니다."),
    CHECK_LIST_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CH-006", "체크리스트 선택지를 찾을 수 없습니다."),
    CHECK_LIST_OPTION_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CH-007", "체크리스트 파일을 찾을 수 없습니다."),
    INVALID_CHECK_LIST_UPDATE_COMMAND(HttpStatus.BAD_REQUEST, "CH-008", "지원하지 않는 체크리스트 작업 유형입니다."),

    // 체크리스트 업데이트 검증
    CHECK_LIST_CREATE_CANNOT_HAVE_ID(HttpStatus.BAD_REQUEST, "CH-009", "새로 생성하는 항목은 ID를 포함할 수 없습니다."),
    CHECK_LIST_UPDATE_REQUIRES_ID(HttpStatus.BAD_REQUEST, "CH-010", "수정할 항목의 ID가 필요합니다."),
    CHECK_LIST_DELETE_REQUIRES_ID(HttpStatus.BAD_REQUEST, "CH-011", "삭제할 항목의 ID가 필요합니다."),
    CHECK_LIST_CREATE_REQUIRES_TITLE_AND_ORDER(HttpStatus.BAD_REQUEST, "CH-012", "새 항목 생성시 제목과 순서는 필수입니다."),
    CHECK_LIST_CREATE_REQUIRES_CONTENT_AND_ORDER(HttpStatus.BAD_REQUEST, "CH-013", "새 선택지 생성시 내용과 순서는 필수입니다."),
    CHECK_LIST_CREATE_REQUIRES_FILE_INFO(HttpStatus.BAD_REQUEST, "CH-014", "새 파일 생성시 파일 정보는 필수입니다."),
    CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST(HttpStatus.BAD_REQUEST, "CH-015", "요청한 체크리스트에 속한 항목이 아닙니다."),
    INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "CH-016", "댓글 내용이 없습니다."),
    NOT_EXISTS_CHECK_LIST_ITEM_COMMENT(HttpStatus.BAD_REQUEST, "CH-017", "댓글이 존재하지 않습니다."),
    NOT_MATCHED_CHECK_LIST_ITEM_COMMENT(HttpStatus.BAD_REQUEST, "CH-018", "요청한 체크리스트 항목의 댓글이 아닙니다."),
    NOT_AUTHORIZED_CHECK_LIST_ITEM_COMMENT_USER(HttpStatus.FORBIDDEN, "CH-019", "댓글 작성자만 수정할 수 있습니다."),
    CHECK_LIST_FILE_MAPPING_NOT_FOUND(HttpStatus.BAD_REQUEST, "CH-020", "요청한 파일 정보를 찾을 수 없습니다."),
    ALREADY_DELETED_CHECK_LIST_ITEM_COMMENT(HttpStatus.BAD_REQUEST, "CH-021", "이미 삭제된 체크리스트 댓글입니다.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
