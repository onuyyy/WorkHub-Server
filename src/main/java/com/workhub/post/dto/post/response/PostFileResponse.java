package com.workhub.post.dto.post.response;

import com.workhub.post.entity.PostFile;

public record PostFileResponse(
        Long postFileId,
        String fileName,
        String originalFileName,
        Integer fileOrder
) {
    public static PostFileResponse from(PostFile file) {
        return new PostFileResponse(
                file.getPostFileId(),
                file.getFileName(),
                file.getOriginalFileName(),
                file.getFileOrder()
        );
    }
}
