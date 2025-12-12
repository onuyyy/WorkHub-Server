package com.workhub.file.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 프로필 이미지 관련 비즈니스 로직을 처리하는 서비스.
 * S3Service를 사용하여 실제 파일 저장소와 통신합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private static final String PROFILE_IMAGE_PREFIX = "profile-images/";
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");

    private final S3Service s3Service;

    /**
     * 프로필 이미지를 업로드하고 public URL을 반환.
     * @param file 업로드할 프로필 이미지 파일
     * @return 업로드된 파일의 public URL
     * @throws BusinessException 파일 검증 실패 또는 업로드 실패 시
     */
    public String uploadProfileImage(MultipartFile file) {
        validateProfileImage(file);

        String fileName = generateProfileImageKey(file.getOriginalFilename());
        s3Service.uploadToS3(file, fileName);

        log.info("프로필 이미지 업로드 성공 : {}", fileName);
        return s3Service.getPublicUrl(fileName);
    }

    /**
     * public URL에서 프로필 이미지를 삭제.
     * @param publicUrl 삭제할 프로필 이미지의 public URL
     * @throws BusinessException URL 파싱 실패 또는 삭제 실패 시
     */
    public void deleteProfileImageByUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            log.warn("삭제할 이미지의 url이 없습니다.");
            return;
        }

        String fileName = s3Service.extractFileNameFromUrl(publicUrl);
        s3Service.deleteFromS3(fileName);
        log.info("프로필 이미지 완전 삭제 성공 : {}", fileName);
    }

    /**
     * 프로필 이미지 파일을 검증.
     * @param file 검증할 파일
     * @throws BusinessException 파일이 유효하지 않을 경우
     */
    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }

        validateImageExtension(originalFilename);
    }

    /**
     * 이미지 파일 확장자를 검증.
     * @param originalFilename 원본 파일명
     * @throws BusinessException 지원하지 않는 이미지 형식일 경우
     */
    private void validateImageExtension(String originalFilename) {
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 프로필 이미지의 S3 key를 생성.
     * @param originalFilename 원본 파일명
     * @return profile-images/ prefix를 포함한 고유 파일명
     */
    private String generateProfileImageKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return PROFILE_IMAGE_PREFIX + UUID.randomUUID() + extension;
    }
}
