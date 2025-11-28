package com.workhub.cs.dto;

import com.workhub.cs.entity.CsPostFile;

public record CsPostFileResponse(
        Long csPostFileId,
        String fileName,
        Integer fileOrder
) {
    public static CsPostFileResponse from(CsPostFile file) {
        return new CsPostFileResponse(
                file.getCsPostFileId(),
                file.getFileName(),
                file.getFileOrder()
        );
    }
}
