package com.workhub.file.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${file.upload.max-size}")
    private long maxFileSize;

    /**
     * 클라이언트로부터 전달받은 파일을 AWS S3에 저장.
     * 파일명은 UUID를 사용하여 고유하게 생성되며, 원본 파일의 확장자는 유지.
     * @param file 업로드할 MultipartFile 객체
     * @return S3에 저장된 고유 파일명 (UUID + 확장자)
     */
    public String uploadFile(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            log.error("File upload failed: Original filename is missing or empty.");
            // 파일명이 없거나 비어있는 것은 잘못된 입력으로 간주.
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }

        validateFileSize(file);
        validateFileExtension(originalFilename);
        String fileName = generateFileName(originalFilename);

        uploadToS3(file, fileName);

        log.info("AWS S3 File Upload Successfully, fileName : {}", fileName);
        return fileName;
    }

    /**
     * S3에 저장된 파일에 대한 서명된 URL을 생성.
     * 파일이 존재하지 않으면 예외 발생.
     * 생성된 URL은 10분간 유효하며, 해당 시간 동안 파일 다운로드가 가능.
     * @param fileName S3에 저장된 파일명
     * @return 10분간 유효한 사전 서명된 다운로드 URL
     */
    public String getPresignedUrl(String fileName) {

        validateFileExists(fileName);

        GetObjectPresignRequest presignRequest = createPresignRequest(fileName);

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    /**
     * 파일을 S3에 업로드.
     * @param file 업로드할 MultipartFile 객체
     * @param fileName S3에 저장될 파일명
     * @throws BusinessException S3 업로드 실패 시
     */
    private void uploadToS3(MultipartFile file, String fileName) {

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build()
                    , RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            log.error("AWS S3 File Upload Failed, fileName : {}, error : {}", fileName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        } catch (S3Exception e) {
            log.error("AWS S3 File Upload Failed, error : {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /**
     * Presigned URL 생성을 위한 GetObjectPresignRequest 생성.
     * @param fileName S3에 저장된 파일명
     * @return 10분간 유효한 GetObjectPresignRequest 객체
     */
    private GetObjectPresignRequest createPresignRequest(String fileName) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        return GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(10))  // 10분간 유효
                .build();
    }

    /**
     * 원본 파일명으로부터 고유한 파일명을 생성.
     * UUID를 사용하여 파일명 중복을 방지하며, 원본 파일의 확장자는 유지.
     * @param originalFilename 원본 파일명
     * @return UUID + 확장자로 구성된 고유 파일명
     */
    private String generateFileName(String originalFilename) {

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * S3에 파일이 존재하는지 확인.
     * @param fileName 확인할 파일명
     * @throws BusinessException 파일이 존재하지 않거나 접근 실패 시
     */
    private void validateFileExists(String fileName) {

        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build()
            );
        } catch (NoSuchKeyException e) {
            log.error("AWS S3 File not found in S3 : {}", fileName);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        } catch (S3Exception e) {
            log.error("AWS S3 error while checking file : {}", fileName, e);
            throw new BusinessException(ErrorCode.FILE_ACCESS_FAIL);
        }
    }

    /**
     * 업로드 하려는 파일의 크기가 허용된 크기를 초과하는지 검증.
     * @param file 업로드할 MultipartFile 객체
     * @throws BusinessException 파일 크기가 허용된 크기를 초과할 경우
     */
    private void validateFileSize(MultipartFile file) {

        if (file.getSize() > maxFileSize) {
            log.error("File size exceeds maximum allowed size. File size: {} bytes, Max size: {} bytes",
                    file.getSize(), maxFileSize);
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 업로드 하려는 파일이 혀용된 파일 확장자인지 검증.
     * @param originalFilename 원본 파일명
     * @throws BusinessException 파일의 확장자가 지원하지 않는 확장자일 경우
     */
    private void validateFileExtension(String originalFilename) {

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        List<String> allowedExtensions = List.of(
                ".jpg", ".jpeg", ".png", ".pdf", ".gif", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".hwp", ".hwpx"
        );

        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
