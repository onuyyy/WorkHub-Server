package com.workhub.post.dto.post.request;

public record PostFileUpdateRequest(
        Long fileId,
        String fileName,
        Integer fileOrder,
        boolean deleted
) {
}