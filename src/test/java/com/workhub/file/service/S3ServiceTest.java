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
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private static final String TEST_REGION = "ap-northeast-2";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", TEST_BUCKET);
        ReflectionTestUtils.setField(s3Service, "region", TEST_REGION);
    }

    @Test
    @DisplayName("S3에 파일 업로드 성공")
    void uploadToS3_success() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );
        String key = "path/test.txt";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // when
        s3Service.uploadToS3(file, key);

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(request.key()).isEqualTo(key);
        assertThat(request.contentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("파일 업로드 시 IOException 발생 시 비즈니스 예외 변환")
    void uploadToS3_ioException_throwBusinessException() throws Exception {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getInputStream()).thenThrow(new IOException("io failure"));

        // when & then
        assertThatThrownBy(() -> s3Service.uploadToS3(file, "test.txt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAIL);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("파일 업로드 시 S3 예외 발생 시 비즈니스 예외 변환")
    void uploadToS3_s3Exception_throwBusinessException() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when & then
        assertThatThrownBy(() -> s3Service.uploadToS3(file, "test.txt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAIL);
    }

    @Test
    @DisplayName("S3에서 파일 삭제 성공")
    void deleteFromS3_success() {
        // given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // when
        s3Service.deleteFromS3("file.txt");

        // then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("파일 삭제 시 S3 예외 발생 시 비즈니스 예외 변환")
    void deleteFromS3_s3Exception_throwBusinessException() {
        // given
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("delete fail").build());

        // when & then
        assertThatThrownBy(() -> s3Service.deleteFromS3("file.txt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_DELETE_FAIL);
    }

    @Test
    @DisplayName("S3에 파일이 존재하면 true 반환")
    void fileExists_trueWhenHeadSucceeds() {
        // given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        // when
        boolean exists = s3Service.fileExists("file.txt");

        // then
        assertThat(exists).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("S3에 파일이 없으면 false 반환")
    void fileExists_falseWhenNoSuchKey() {
        // given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // when
        boolean exists = s3Service.fileExists("missing.txt");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("파일 존재 확인 시 S3 예외 발생 시 비즈니스 예외 변환")
    void fileExists_s3Exception_throwBusinessException() {
        // given
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("access error").build());

        // when & then
        assertThatThrownBy(() -> s3Service.fileExists("file.txt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_ACCESS_FAIL);
    }

    @Test
    @DisplayName("Public URL 포맷 생성")
    void getPublicUrl_buildsUrlCorrectly() {
        // when
        String url = s3Service.getPublicUrl("folder/file.txt");

        // then
        assertThat(url).isEqualTo("https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/folder/file.txt");
    }

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void createPresignedUrl_success() throws Exception {
        // given
        String expectedUrl = "https://example.com/presigned";
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL(expectedUrl));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presigned);

        // when
        String presignedUrl = s3Service.createPresignedUrl("file.txt", Duration.ofMinutes(5));

        // then
        assertThat(presignedUrl).isEqualTo(expectedUrl);

        ArgumentCaptor<GetObjectPresignRequest> captor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner).presignGetObject(captor.capture());
        assertThat(captor.getValue().getObjectRequest().bucket()).isEqualTo(TEST_BUCKET);
        assertThat(captor.getValue().getObjectRequest().key()).isEqualTo("file.txt");
    }

    @Test
    @DisplayName("Public URL에서 파일명 추출 성공")
    void extractFileNameFromUrl_success() {
        // given
        String publicUrl = "https://" + TEST_BUCKET + ".s3." + TEST_REGION + ".amazonaws.com/path/file.txt";

        // when
        String key = s3Service.extractFileNameFromUrl(publicUrl);

        // then
        assertThat(key).isEqualTo("path/file.txt");
    }

    @Test
    @DisplayName("잘못된 Public URL 포맷이면 예외 발생")
    void extractFileNameFromUrl_invalidUrl_throwBusinessException() {
        // when & then
        assertThatThrownBy(() -> s3Service.extractFileNameFromUrl("https://example.com/file.txt"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_URL);
    }
}
