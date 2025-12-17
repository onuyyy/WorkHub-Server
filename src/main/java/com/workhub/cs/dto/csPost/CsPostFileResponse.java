package com.workhub.cs.dto.csPost;

import com.workhub.cs.entity.CsPostFile;

public record CsPostFileResponse(
        Long csPostFileId,
        String fileName,
        String originalFileName,
        Integer fileOrder
) {
    public static CsPostFileResponse from(CsPostFile file) {
        return new CsPostFileResponse(
                file.getCsPostFileId(),
                file.getFileName(),
                file.getOriginalFileName(),
                file.getFileOrder()
        );
    }
}
