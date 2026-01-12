package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckListTemplate;

import java.util.List;

public record CheckListTemplateResponse(
        Long templateId,
        String itemTitle,
        String description,
        List<CheckListItemResponse> items
) {

    /**
     * 템플릿 목록 조회용 (계층 구조 없이 기본 정보만)
     */
    public static CheckListTemplateResponse from(CheckListTemplate template) {
        return new CheckListTemplateResponse(
                template.getTemplateId(),
                template.getItemTitle(),
                template.getDescription(),
                null
        );
    }

    /**
     * 템플릿 단건 조회용 (전체 계층 구조 포함)
     */
    public static CheckListTemplateResponse withItems(
            CheckListTemplate template,
            List<CheckListItemResponse> items
    ) {
        return new CheckListTemplateResponse(
                template.getTemplateId(),
                template.getItemTitle(),
                template.getDescription(),
                items
        );
    }
}
