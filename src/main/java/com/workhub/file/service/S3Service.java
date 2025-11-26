package com.workhub.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 클라이언트로부터 전달받은 파일을 AWS S3에 저장.
     * 파일명은 UUID를 사용하여 고유하게 생성되며, 원본 파일의 확장자는 유지.
     *
     * @param file 업로드할 MultipartFile 객체
     * @return S3에 저장된 고유 파일명 (UUID + 확장자)
     * @throws IOException 파일 입력 스트림 읽기 실패 시 발생
     */
    public String uploadFile(MultipartFile file) throws IOException {

        String fileName =generateFileName(file.getOriginalFilename());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build()
                , RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        log.info("AWS S3 File Upload Successfully, fileName {}", fileName);
        return fileName;
    }

    /**
     * S3에 저장된 파일에 대한 서명된 URL을 생성.
     * 생성된 URL은 10분간 유효하며, 해당 시간 동안 파일 다운로드가 가능.
     *
     * @param fileName S3에 저장된 파일명
     * @return 10분간 유효한 사전 서명된 다운로드 URL
     */
    public String getPresignedUrl(String fileName) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(10))  // 10분간 유효
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    /**
     * 원본 파일명으로부터 고유한 파일명을 생성.
     * UUID를 사용하여 파일명 중복을 방지하며, 원본 파일의 확장자는 유지.
     *
     * @param originalFilename 원본 파일명
     * @return UUID + 확장자로 구성된 고유 파일명
     */
    private String generateFileName(String originalFilename) {

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

}
