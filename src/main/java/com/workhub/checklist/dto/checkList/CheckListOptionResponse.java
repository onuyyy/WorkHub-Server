package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckListOption;

import java.util.List;

public record CheckListOptionResponse(
        Long checkListOptionId,
        String optionContent,
        Integer optionOrder,
        List<CheckListOptionFileResponse> files
) {
    public static CheckListOptionResponse from(CheckListOption option, List<CheckListOptionFileResponse> files) {
        return new CheckListOptionResponse(
                option.getCheckListOptionId(),
                option.getOptionContent(),
                option.getOptionOrder(),
                files
        );
    }
}
