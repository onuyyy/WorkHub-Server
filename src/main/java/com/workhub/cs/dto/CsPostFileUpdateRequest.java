package com.workhub.cs.dto;

public record CsPostFileUpdateRequest(
        Long fileId,
        String fileUrl,
        String fileName,
        Integer fileOrder,
        boolean deleted
) {
}
