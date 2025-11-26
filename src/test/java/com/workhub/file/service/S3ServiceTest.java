package com.workhub.file.service;

import com.workhub.file.dto.FileUploadResponse;
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

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

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
    @DisplayName("정상적인 파일 업로드 - 단일 파일")
    void uploadFiles_singleFile_success() {
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
        List<FileUploadResponse> responses = s3Service.uploadFiles(List.of(file));

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).fileName()).endsWith(".jpg");
        assertThat(responses.get(0).presignedUrl()).isEmpty();
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("정상적인 파일 업로드 - 여러 파일")
    void uploadFiles_multipleFiles_success() {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test1.jpg",
                "image/jpeg",
                "test content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test2.pdf",
                "application/pdf",
                "test content 2".getBytes()
        );

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        List<FileUploadResponse> responses = s3Service.uploadFiles(List.of(file1, file2));

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).fileName()).endsWith(".jpg");
        assertThat(responses.get(1).fileName()).endsWith(".pdf");
        verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("빈 파일 리스트 업로드")
    void uploadFiles_emptyList_returnEmptyList() {
        // when
        List<FileUploadResponse> responses = s3Service.uploadFiles(List.of());

        // then
        assertThat(responses).isEmpty();
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("null 파일 리스트 업로드")
    void uploadFiles_nullList_returnEmptyList() {
        // when
        List<FileUploadResponse> responses = s3Service.uploadFiles(null);

        // then
        assertThat(responses).isEmpty();
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생")
    void uploadFiles_emptyFile_throwException() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFiles(List.of(emptyFile)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("파일명이 없는 파일 업로드 시 예외 발생")
    void uploadFiles_noFileName_throwException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "image/jpeg",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFiles(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("파일 크기 초과 시 예외 발생")
    void uploadFiles_fileSizeExceeded_throwException() {
        // given
        byte[] largeContent = new byte[(int) (MAX_FILE_SIZE + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFiles(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("허용되지 않은 파일 확장자 업로드 시 예외 발생")
    void uploadFiles_invalidExtension_throwException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3Service.uploadFiles(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    @DisplayName("S3 업로드 실패 시 예외 발생")
    void uploadFiles_s3Exception_throwException() {
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
        assertThatThrownBy(() -> s3Service.uploadFiles(List.of(file)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAIL);
    }

    @Test
    @DisplayName("허용된 모든 파일 확장자 업로드 성공")
    void uploadFiles_allAllowedExtensions_success() {
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

            List<FileUploadResponse> responses = s3Service.uploadFiles(List.of(file));

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).fileName()).endsWith(extension);
        }
    }

    @Test
    @DisplayName("정상적으로 단일 파일의 Presigned URL 생성")
    void getPresignedUrls_singleFile_success() throws Exception {
        // given
        String fileName = "test.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/test.jpg?signed=true";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build());

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(new URL(expectedUrl));
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when
        List<FileUploadResponse> responses = s3Service.getPresignedUrls(List.of(fileName));

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).fileName()).isEqualTo(fileName);
        assertThat(responses.get(0).presignedUrl()).isEqualTo(expectedUrl);
        verify(s3Client, times(1)).headObject(any(HeadObjectRequest.class));
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("정상적으로 여러 파일의 Presigned URL 생성")
    void getPresignedUrls_multipleFiles_success() throws Exception {
        // given
        List<String> fileNames = List.of("file1.jpg", "file2.pdf", "file3.png");

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build());

        PresignedGetObjectRequest presignedRequest1 = mock(PresignedGetObjectRequest.class);
        PresignedGetObjectRequest presignedRequest2 = mock(PresignedGetObjectRequest.class);
        PresignedGetObjectRequest presignedRequest3 = mock(PresignedGetObjectRequest.class);

        given(presignedRequest1.url()).willReturn(new URL("https://test-bucket.s3.amazonaws.com/file1.jpg?signed=true"));
        given(presignedRequest2.url()).willReturn(new URL("https://test-bucket.s3.amazonaws.com/file2.pdf?signed=true"));
        given(presignedRequest3.url()).willReturn(new URL("https://test-bucket.s3.amazonaws.com/file3.png?signed=true"));

        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest1, presignedRequest2, presignedRequest3);

        // when
        List<FileUploadResponse> responses = s3Service.getPresignedUrls(fileNames);

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).fileName()).isEqualTo("file1.jpg");
        assertThat(responses.get(0).presignedUrl()).contains("file1.jpg");
        assertThat(responses.get(1).fileName()).isEqualTo("file2.pdf");
        assertThat(responses.get(1).presignedUrl()).contains("file2.pdf");
        assertThat(responses.get(2).fileName()).isEqualTo("file3.png");
        assertThat(responses.get(2).presignedUrl()).contains("file3.png");
        verify(s3Client, times(3)).headObject(any(HeadObjectRequest.class));
        verify(s3Presigner, times(3)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("빈 파일명 리스트로 Presigned URL 요청 시 빈 리스트 반환")
    void getPresignedUrls_emptyList_returnEmptyList() {
        // when
        List<FileUploadResponse> responses = s3Service.getPresignedUrls(List.of());

        // then
        assertThat(responses).isEmpty();
        verify(s3Client, never()).headObject(any(HeadObjectRequest.class));
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 파일의 Presigned URL 요청 시 예외 발생")
    void getPresignedUrls_fileNotFound_throwException() {
        // given
        String fileName = "non-existent.jpg";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("File not found").build());

        // when & then
        assertThatThrownBy(() -> s3Service.getPresignedUrls(List.of(fileName)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("S3 접근 실패 시 예외 발생")
    void getPresignedUrls_s3AccessFail_throwException() {
        // given
        String fileName = "test.jpg";

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(S3Exception.builder().message("Access denied").build());

        // when & then
        assertThatThrownBy(() -> s3Service.getPresignedUrls(List.of(fileName)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_ACCESS_FAIL);

        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("여러 파일 중 하나가 존재하지 않으면 예외 발생")
    void getPresignedUrls_oneFileNotFound_throwException() throws Exception {
        // given
        List<String> fileNames = List.of("file1.jpg", "non-existent.pdf");

        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build())
                .willThrow(NoSuchKeyException.builder().message("File not found").build());

        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        given(presignedRequest.url()).willReturn(new URL("https://test-bucket.s3.amazonaws.com/file1.jpg"));
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(presignedRequest);

        // when & then
        assertThatThrownBy(() -> s3Service.getPresignedUrls(fileNames))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }
}