package com.workhub.checklist.dto.checkList;

import com.workhub.checklist.entity.checkList.CheckListOptionFile;

public record CheckListOptionFileResponse(
        Long checkListOptionFileId,
        String fileUrl,
        String fileName,
        Integer fileOrder
) {
    public static CheckListOptionFileResponse from(CheckListOptionFile file) {
        return new CheckListOptionFileResponse(
                file.getCheckListOptionFileId(),
                file.getFileUrl(),
                file.getFileName(),
                file.getFileOrder()
        );
    }
}