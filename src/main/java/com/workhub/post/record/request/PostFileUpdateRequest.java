package com.workhub.post.record.request;

public record PostFileUpdateRequest(
        Long fileId,
        String fileName,
        Integer fileOrder,
        boolean deleted
) {
}