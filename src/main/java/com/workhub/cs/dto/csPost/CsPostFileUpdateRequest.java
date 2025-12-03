package com.workhub.cs.dto.csPost;

public record CsPostFileUpdateRequest(
        Long fileId,
        String fileName,
        Integer fileOrder,
        boolean deleted
) {
}
