package com.workhub.file.service;

import com.workhub.global.util.SecurityUtil;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateProfileService {

    private final ProfileImageService profileImageService;
    private final UserService userService;

    public String updateProfile(MultipartFile file) {

        Long userID = SecurityUtil.getCurrentUserIdOrThrow();
        UserTable user = userService.getUserById(userID);

        String oldProfile = user.getProfileImg();
        String newProfile = profileImageService.uploadProfileImage(file);

        user.updateProfile(newProfile);

        // 기존 프로필 이미지가 있으면 삭제
        if (oldProfile != null && !oldProfile.isBlank()) {
            profileImageService.deleteProfileImageByUrl(oldProfile);
        }

        return newProfile;
    }
}
