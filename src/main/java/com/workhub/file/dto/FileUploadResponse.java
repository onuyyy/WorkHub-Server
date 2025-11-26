package com.workhub.file.dto;

public record FileUploadResponse(String fileName, String presignedUrl) {

    public static FileUploadResponse from(String fileName, String presignedUrl) {
        return new FileUploadResponse(fileName, presignedUrl);
    }
}
