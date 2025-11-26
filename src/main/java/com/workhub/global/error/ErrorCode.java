package com.workhub.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // test
    TEST(HttpStatus.BAD_REQUEST, "001", "test error"),

    // server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "002", "서버에러, 에러가 계속될경우 담당자에게 문의 바랍니다."),

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

    // 게시물
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PO-001", "게시물을 찾을 수 없습니다."),
    // CS 게시판
    NOT_EXISTS_CS_POST(HttpStatus.BAD_REQUEST, "C-001", "존재하지 않는 CS 게시물입니다."),

    // AWS S3
    // 파일 관련 에러
    FILE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-001", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-002", "파일 삭제에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F-003", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F-004", "지원하지 않는 파일 형식입니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "F-005", "파일 이름이 누락되었습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F-006", "파일 크기가 제한을 초과했습니다."),
    FILE_ACCESS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "F-007", "파일 접근에 실패했습니다."),;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }
}
