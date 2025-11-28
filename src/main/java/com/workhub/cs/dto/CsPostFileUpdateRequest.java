package com.workhub.cs.dto;

public record CsPostFileUpdateRequest(
        Long fileId,
        String fileName,
        Integer fileOrder,
        boolean deleted
) {
}
