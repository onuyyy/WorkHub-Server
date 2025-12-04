package com.workhub.post.record.response;

import com.workhub.post.entity.PostFile;

public record PostFileResponse(
        Long postFileId,
        String fileName,
        Integer fileOrder
) {
    public static PostFileResponse from(PostFile file) {
        return new PostFileResponse(
                file.getPostFileId(),
                file.getFileName(),
                file.getFileOrder()
        );
    }
}
