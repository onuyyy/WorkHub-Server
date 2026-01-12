package com.workhub.post.dto.post.request;

import com.workhub.file.dto.FileUploadResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PostFileRequest(
        @NotBlank String fileUrl,
        @NotBlank String fileName,
        String originalFileName,
        Integer fileOrder
) {
    public static PostFileRequest from(FileUploadResponse uploadFile, int order) {
        return PostFileRequest.builder()
                .fileUrl(uploadFile.fileName())
                .fileName(uploadFile.fileName())
                .originalFileName(uploadFile.originalFileName())
                .fileOrder(order)
                .build();
    }
}
