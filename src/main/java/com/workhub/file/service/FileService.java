package com.workhub.file.service;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * 일반 파일 관련 비즈니스 로직을 처리하는 서비스.
 * S3Service를 사용하여 실제 파일 저장소와 통신합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".pdf", ".gif",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".hwp", ".hwpx"
    );
    private static final Duration PRESIGNED_URL_DURATION = Duration.ofMinutes(10);

    private final S3Service s3Service;

    /**
     * 여러 파일을 업로드.
     * @param files 업로드할 파일 리스트
     * @return 업로드된 파일명 리스트
     */
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .map(file -> {
                    if (file.isEmpty()) {
                        log.error("파일 업로드 실패 : 업로드할 파일이 없습니다.");
                        throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
                    }
                    String fileName = uploadFile(file);
                    String originalFileName = file.getOriginalFilename();
                    return FileUploadResponse.from(fileName, originalFileName, "");
                })
                .toList();
    }

    /**
     * 파일명 리스트에 대한 Presigned URL을 생성.
     * @param fileNames S3에 저장된 파일명 리스트
     * @return Presigned URL이 포함된 FileUploadResponse 리스트
     */
    public List<FileUploadResponse> getDownloadUrls(List<String> fileNames) {
        return fileNames.stream()
                .map(this::getDownloadUrl)
                .toList();
    }

    /**
     * 파일명과 원본 파일명으로 다운로드용 Presigned URL을 생성.
     * @param fileName S3에 저장된 파일명
     * @param originalFileName 다운로드 시 사용할 원본 파일명
     * @return Presigned URL이 포함된 FileUploadResponse
     */
    public FileUploadResponse getDownloadUrlWithOriginalName(String fileName, String originalFileName) {
        if (!s3Service.fileExists(fileName)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        String presignedUrl = s3Service.createPresignedUrlForDownload(
                fileName,
                PRESIGNED_URL_DURATION,
                originalFileName
        );
        return FileUploadResponse.from(fileName, originalFileName, presignedUrl);
    }

    /**
     * S3에 저장된 파일을 삭제.
     * @param fileName 삭제할 파일명
     * @throws BusinessException 파일명이 유효하지 않거나 삭제 실패 시
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("삭제할 파일명이 없습니다.");
            return;
        }

        s3Service.deleteFromS3(fileName);
        log.info("파일 삭제 성공: {}", fileName);
    }

    /**
     * S3에 저장된 여러 파일을 삭제.
     * @param fileNames 삭제할 파일명 리스트
     */
    public void deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            log.warn("삭제할 파일 목록이 비어있습니다.");
            return;
        }

        fileNames.forEach(this::deleteFile);
        log.info("파일 {} 개 삭제 완료", fileNames.size());
    }

    /**
     * 단일 파일을 업로드.
     * @param file 업로드할 파일
     * @return S3에 저장된 파일명
     */
    private String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            log.error("파일 업로드 실패 : 원본 파일명이 없습니다.");
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }

        validateFileExtension(originalFilename);
        String fileName = generateFileName(originalFilename);

        s3Service.uploadToS3(file, fileName);

        log.info("파일 업로드 성공 : {}", fileName);
        return fileName;
    }

    /**
     * 단일 파일에 대한 Presigned URL을 생성.
     * @param fileName S3에 저장된 파일명
     * @return Presigned URL이 포함된 FileUploadResponse
     */
    private FileUploadResponse getDownloadUrl(String fileName) {
        if (!s3Service.fileExists(fileName)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        String presignedUrl = s3Service.createPresignedUrl(fileName, PRESIGNED_URL_DURATION);
        return FileUploadResponse.from(fileName, fileName, presignedUrl);
    }

    /**
     * 파일 확장자를 검증.
     * @param originalFilename 원본 파일명
     * @throws BusinessException 지원하지 않는 파일 형식일 경우
     */
    private void validateFileExtension(String originalFilename) {
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 고유한 파일명을 생성.
     * @param originalFilename 원본 파일명
     * @return UUID + 확장자로 구성된 고유 파일명
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return UUID.randomUUID() + extension;
    }
}
