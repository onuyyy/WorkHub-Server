package com.workhub.file.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    private static final String TEST_BUCKET = "test-bucket";
    private static final long MAX_FILE_SIZE = 10485760L; // 10MB

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", TEST_BUCKET);
        ReflectionTestUtils.setField(s3Service, "maxFileSize", MAX_FILE_SIZE);
    }

    @Test
    @DisplayName("정상적인 파일 업로드 시 파일명을 반환한다")
    void uploadFile_success_shouldReturnFileName() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        String fileName = s3Service.uploadFile(file);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName).endsWith(".jpg");
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("파일 크기가 최대 크기를 초과하면 예외를 던진다")
    void uploadFile_fileSizeExceeded_shouldThrow() {
        // given
        byte[] largeContent = new byte[(int) (MAX_FILE_SIZE + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_SIZE_EXCEEDED);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("허용되지 않은 파일 확장자면 예외를 던진다")
    void uploadFile_invalidExtension_shouldThrow() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("S3 업로드 실패 시 예외를 던진다")
    void uploadFile_s3Exception_shouldThrow() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(S3Exception.builder().message("S3 error").build());

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAIL);
    }

    @Test
    @DisplayName("정상적으로 Presigned URL을 생성한다")
    void getPresignedUrl_success_shouldReturnUrl() throws Exception {
        // given
        String fileName = "test-file.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/test-file.jpg?signed=true";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build());

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(URI.create(expectedUrl).toURL());
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        String url = s3Service.getPresignedUrl(fileName);

        // then
        assertThat(url).isEqualTo(expectedUrl);
        verify(s3Client, times(1)).headObject(any(HeadObjectRequest.class));
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 파일의 Presigned URL 요청 시 예외를 던진다")
    void getPresignedUrl_fileNotFound_shouldThrow() {
        // given
        String fileName = "non-existent.jpg";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("File not found").build());

        // when & then
        assertThatThrownBy(() -> s3Service.getPresignedUrl(fileName))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("S3 접근 실패 시 예외를 던진다")
    void getPresignedUrl_s3AccessFail_shouldThrow() {
        // given
        String fileName = "test.jpg";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(S3Exception.builder().message("Access denied").build());

        // when & then
        assertThatThrownBy(() -> s3Service.getPresignedUrl(fileName))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_ACCESS_FAIL);
    }

    @Test
    @DisplayName("허용된 모든 확장자의 파일을 업로드할 수 있다")
    void uploadFile_allAllowedExtensions_shouldSuccess() {
        // given
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".pdf", ".gif",
                ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".hwp", ".hwpx"};

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when & then
        for (String extension : allowedExtensions) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test" + extension,
                    "application/octet-stream",
                    "test content".getBytes()
            );

            String fileName = s3Service.uploadFile(file);

            assertThat(fileName).endsWith(extension);
        }
    }

    @Test
    @DisplayName("파일명이 없는 파일은 업로드 시 예외를 던진다")
    void uploadFile_noFileName_shouldThrowException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "", // 파일명이 빈 문자열
                "image/jpeg",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);

        // S3 클라이언트는 호출되지 않아야 함
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("대소문자가 섞인 확장자도 정상적으로 처리한다")
    void uploadFile_mixedCaseExtension_shouldSuccess() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.JPG",
                "image/jpeg",
                "test content".getBytes()
        );

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        String fileName = s3Service.uploadFile(file);

        // then
        assertThat(fileName).isNotNull();
        assertThat(fileName.toLowerCase()).endsWith(".jpg");
    }
}