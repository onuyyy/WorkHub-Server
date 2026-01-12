package com.workhub.file.dto;

public record FileUploadResponse(String fileName, String originalFileName, String presignedUrl) {

    public static FileUploadResponse from(String fileName, String originalFileName, String presignedUrl) {
        return new FileUploadResponse(fileName, originalFileName, presignedUrl);
    }
}
