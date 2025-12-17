package com.workhub.cs.dto.csPost;

public record CsPostFileUpdateRequest(
        Long fileId,
        String fileName,
        String fileUrl,
        Integer fileOrder,
        boolean deleted
) {
}
