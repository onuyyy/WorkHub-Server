package com.workhub.checklist.dto.checkList;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.validation.constraints.NotNull;

public record CheckListOptionFileUpdateRequest(
        @NotNull(message = "파일 작업 유형은 필수입니다")
        CheckListUpdateCommandType changeType,

        Long checkListOptionFileId,
        String fileUrl,
        Integer fileOrder
) {
    /**
     * changeType에 따른 필드 검증
     * - CREATE: ID 없어야 함, fileUrl 필수
     * - UPDATE: ID 필수
     * - DELETE: ID 필수
     */
    public void validate() {
        if (changeType == null) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }

        switch (changeType) {
            case CREATE -> {
                if (checkListOptionFileId != null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_CREATE_CANNOT_HAVE_ID);
                }
                if (fileUrl == null || fileUrl.isBlank()) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_CREATE_REQUIRES_FILE_INFO);
                }
            }
            case UPDATE -> {
                if (checkListOptionFileId == null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_UPDATE_REQUIRES_ID);
                }
                // UPDATE는 부분 업데이트 허용: fileUrl, fileOrder 중 하나만 있어도 OK
            }
            case DELETE -> {
                if (checkListOptionFileId == null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_DELETE_REQUIRES_ID);
                }
            }
        }
    }
}
