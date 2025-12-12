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

/**
 * AWS S3와의 직접적인 통신을 담당하는 저수준 서비스.
 * 비즈니스 로직 없이 순수한 S3 작업만 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * S3에 파일을 업로드.
     * @param file 업로드할 MultipartFile 객체
     * @param key S3에 저장될 키 (파일명, prefix 포함 가능)
     * @throws BusinessException S3 업로드 실패 시
     */
    public void uploadToS3(MultipartFile file, String key) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("S3 upload successful: {}", key);
        } catch (IOException e) {
            log.error("S3 upload failed - IOException: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        } catch (S3Exception e) {
            log.error("S3 upload failed - S3Exception: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
        }
    }

    /**
     * S3에서 파일을 삭제.
     * @param key 삭제할 파일의 키
     * @throws BusinessException S3 파일 삭제 실패 시
     */
    public void deleteFromS3(String key) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            log.info("S3 delete successful: {}", key);
        } catch (S3Exception e) {
            log.error("S3 delete failed: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAIL);
        }
    }

    /**
     * S3에 파일이 존재하는지 확인.
     * @param key 확인할 파일의 키
     * @return 파일 존재 여부
     */
    public boolean fileExists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("File not found in S3: {}", key);
            return false;
        } catch (S3Exception e) {
            log.error("S3 error while checking file existence: {}", key, e);
            throw new BusinessException(ErrorCode.FILE_ACCESS_FAIL);
        }
    }

    /**
     * S3 파일의 public URL을 생성.
     * @param key S3에 저장된 파일의 키
     * @return public URL
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    /**
     * S3 파일의 Presigned URL을 생성.
     * @param key S3에 저장된 파일의 키
     * @param duration URL 유효 기간
     * @return Presigned URL
     */
    public String createPresignedUrl(String key, Duration duration) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(duration)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    /**
     * public URL에서 S3 키(파일명)를 추출.
     * @param publicUrl S3 파일의 public URL
     * @return S3 키 (prefix 포함)
     * @throws BusinessException URL 형식이 올바르지 않을 경우
     */
    public String extractFileNameFromUrl(String publicUrl) {
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (publicUrl != null && publicUrl.startsWith(baseUrl)) {
            return publicUrl.substring(baseUrl.length());
        }
        log.error("Invalid S3 public URL format: {}", publicUrl);
        throw new BusinessException(ErrorCode.INVALID_FILE_URL);
    }
}
