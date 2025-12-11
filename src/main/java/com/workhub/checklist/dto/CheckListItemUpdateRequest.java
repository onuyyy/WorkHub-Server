package com.workhub.checklist.dto;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckListItemUpdateRequest(
        @NotNull(message = "항목 작업 유형은 필수입니다")
        CheckListUpdateCommandType changeType,

        Long checkListItemId,
        String itemTitle,
        Integer itemOrder,
        Long templateId,

        @Valid
        List<CheckListOptionUpdateRequest> options
) {
    /**
     * changeType에 따른 필드 검증
     * - CREATE: ID 없어야 함, title/order 필수
     * - UPDATE: ID 필수
     * - DELETE: ID 필수
     */
    public void validate() {
        if (changeType == null) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }

        switch (changeType) {
            case CREATE -> {
                if (checkListItemId != null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_CREATE_CANNOT_HAVE_ID);
                }
                if (itemTitle == null || itemOrder == null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_CREATE_REQUIRES_TITLE_AND_ORDER);
                }
            }
            case UPDATE -> {
                if (checkListItemId == null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_UPDATE_REQUIRES_ID);
                }
                // UPDATE는 부분 업데이트 허용: title, order 중 하나만 있어도 OK
            }
            case DELETE -> {
                if (checkListItemId == null) {
                    throw new BusinessException(ErrorCode.CHECK_LIST_DELETE_REQUIRES_ID);
                }
            }
        }

        // 하위 options도 검증
        if (options != null) {
            options.forEach(CheckListOptionUpdateRequest::validate);
        }
    }
}
