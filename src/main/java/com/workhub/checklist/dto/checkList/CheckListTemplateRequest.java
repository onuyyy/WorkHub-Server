package com.workhub.checklist.dto.checkList;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckListTemplateRequest(
        @NotBlank(message = "템플릿 제목은 필수입니다.")
        @Size(max = 50, message = "템플릿 제목은 50자 이하여야 합니다.")
        String itemTitle,

        @Size(max = 2000, message = "템플릿 설명은 2000자 이하여야 합니다.")
        String description
) {
    /**
     * CheckListItemRequest로부터 CheckListTemplateRequest를 생성한다.
     * @param itemRequest CheckListItem 생성 요청
     * @param templateTitle 템플릿 제목
     * @param templateDescription 템플릿 설명
     * @return CheckListTemplateRequest
     */
    public static CheckListTemplateRequest from(CheckListItemRequest itemRequest,
                                                 String templateTitle,
                                                 String templateDescription) {
        return new CheckListTemplateRequest(
                templateTitle != null ? templateTitle : itemRequest.itemTitle(),
                templateDescription
        );
    }
}
