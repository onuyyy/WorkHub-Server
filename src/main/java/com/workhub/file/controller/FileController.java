package com.workhub.file.controller;

import com.workhub.file.api.FileApi;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadFile(@RequestPart("file") List<MultipartFile> files) {

        List<FileUploadResponse> fileName = fileService.uploadFiles(files);
        return ApiResponse.success(fileName);
    }

    @GetMapping("/get-files")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getFileUrl(@RequestParam("fileNames") List<String> fileNames) {

        List<FileUploadResponse> presignedUrls = fileService.getDownloadUrls(fileNames);
        return ApiResponse.success(presignedUrls);
    }
}
