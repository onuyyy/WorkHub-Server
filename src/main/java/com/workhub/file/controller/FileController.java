package com.workhub.file.controller;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.S3Service;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = s3Service.uploadFile(file);

        return ApiResponse.success(
                new FileUploadResponse(fileName, "")
        );
    }

    @GetMapping("/{fileName}")
    public ApiResponse<FileUploadResponse> getFileUrl (@PathVariable String fileName) {

        String presignedUrl = s3Service.getPresignedUrl(fileName);
        return ApiResponse.success(new FileUploadResponse(fileName, presignedUrl));
    }
}
