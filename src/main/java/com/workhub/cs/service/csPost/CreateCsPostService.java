package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.cs.port.AuthorLookupPort;
import com.workhub.cs.port.dto.AuthorProfile;
import com.workhub.global.entity.ActionType;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;
    private final CsPostNotificationService csPostNotificationService;
    private final AuthorLookupPort authorLookupPort;
    private final FileService fileService;

    /**
     * CS POST를 작성한다.
     * @param projectId 프로젝트 식별자
     * @param userId 유저 식별자
     * @param request CS 게시글 생성 요청
     * @param files 첨부파일 data
     * @return CsPostResponse
     */
    public CsPostResponse create(Long projectId, Long userId, CsPostRequest request, List<MultipartFile> files) {

        projectService.validateCompletedProject(projectId);

        // 업로드된 파일명을 추적하여 예외 발생 시 삭제
        List<String> uploadedFileNames = new ArrayList<>();

        try {
            CsPost csPost = csPostService.save(CsPost.of(projectId, userId, request));

            // 파일 업로드 + CsPostFile 저장
            List<CsPostFile> savedFiles = uploadAndSaveFiles(csPost.getCsPostId(), files);
            uploadedFileNames = savedFiles.stream()
                    .map(CsPostFile::getFileUrl)
                    .toList();

            csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.CREATE);
            csPostNotificationService.notifyCreated(projectId, csPost.getCsPostId(), csPost.getTitle());

            String userName = authorLookupPort.findByUserId(userId)
                    .map(AuthorProfile::userName)
                    .orElse(null);

            return CsPostResponse.from(csPost, savedFiles, userName);

        } catch (Exception e) {
            // 예외 발생 시 업로드된 S3 파일 삭제 (Best Effort)
            rollbackUploadedFiles(uploadedFileNames);
            // 원래 예외를 다시 throw하여 트랜잭션 롤백 및 사용자에게 에러 응답
            throw e;
        }
    }

    /**
     * 업로드된 S3 파일을 삭제 (Best Effort).
     * 삭제 실패 시에도 예외를 throw하지 않고 로그만 기록합니다.
     * @param uploadedFileNames 삭제할 파일명 리스트
     */
    private void rollbackUploadedFiles(List<String> uploadedFileNames) {
        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return;
        }

        try {
            log.warn("CS 게시글 생성 실패로 인해 업로드된 파일 {} 개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
            log.info("업로드된 파일 {} 개 삭제 완료", uploadedFileNames.size());
        } catch (Exception deleteException) {
            log.error("업로드된 파일 삭제 중 오류 발생 (파일은 S3에 남아있을 수 있음): {}", uploadedFileNames, deleteException);
        }
    }

    /**
     * S3에 파일 업로드 후 CsPostFile 엔티티를 생성하여 저장.
     * @param csPostId CS 게시글 ID
     * @param files 업로드할 파일 리스트
     * @return 저장된 CsPostFile 목록
     */
    private List<CsPostFile> uploadAndSaveFiles(Long csPostId, List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        // S3에 파일 업로드
        List<FileUploadResponse> uploadFiles = fileService.uploadFiles(files);

        // CsPostFile 엔티티 생성 및 저장
        List<CsPostFile> csPostFiles = IntStream.range(0, uploadFiles.size())
                .mapToObj(index -> {
                    FileUploadResponse uploadFile = uploadFiles.get(index);
                    return CsPostFile.of(csPostId, uploadFile, index + 1);
                })
                .toList();

        return csPostService.saveAllFiles(csPostFiles);
    }

}
