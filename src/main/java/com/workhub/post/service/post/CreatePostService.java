package com.workhub.post.service.post;

import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.post.PostHistorySnapshot;
import com.workhub.post.dto.post.request.PostLinkRequest;
import com.workhub.post.dto.post.request.PostRequest;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostLink;
import com.workhub.post.event.PostCreatedEvent;
import com.workhub.post.service.PostValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreatePostService {

    private final PostService postService;
    private final PostValidator postValidator;
    private final HistoryRecorder historyRecorder;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;

    /**
     * 게시글 생성 시 프로젝트 상태와 부모 게시글 유효성을 검증한 뒤 저장한다.
     *
     * @param projectId     프로젝트 ID
     * @param projectNodeId 프로젝트 노드 ID
     * @param userId        작성자 ID
     * @param request       게시글 생성 요청
     * @param files         첨부파일 data
     * @return 저장된 게시글
     */
    public PostResponse create(Long projectId, Long projectNodeId, Long userId, PostRequest request, List<MultipartFile> files) {
        postValidator.validateNodeAndProject(projectNodeId, projectId);

        Long parentPostId = request.parentPostId();
        if (parentPostId != null && !postService.existsActivePost(parentPostId)) {
            throw new BusinessException(ErrorCode.PARENT_POST_NOT_FOUND);
        }
        if (parentPostId != null) {
            Post parent = postService.findById(parentPostId);
            postService.validateNode(parent, projectNodeId);
        }

        // 업로드된 파일명을 추적하여 예외 발생 시 삭제
        List<String> uploadedFileNames = new ArrayList<>();

        try {
            Post savedPost = postService.save(Post.of(projectNodeId, userId, parentPostId, request));

            // 파일 업로드 + PostFile 저장
            List<PostFile> savedFiles = uploadAndSaveFiles(savedPost.getPostId(), files);
            uploadedFileNames = savedFiles.stream()
                    .map(PostFile::getFileUrl)
                    .toList();

            List<PostLink> savedLinks = savePostLinks(savedPost.getPostId(), request.links());

            historyRecorder.recordHistory(HistoryType.POST, savedPost.getPostId(), ActionType.CREATE, PostHistorySnapshot.from(savedPost));
            eventPublisher.publishEvent(new PostCreatedEvent(projectId, savedPost));

            return PostResponse.from(savedPost, savedFiles, savedLinks);

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
            log.warn("게시글 생성 실패로 인해 업로드된 파일 {} 개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
            log.info("업로드된 파일 {} 개 삭제 완료", uploadedFileNames.size());
        } catch (Exception deleteException) {
            log.error("업로드된 파일 삭제 중 오류 발생 (파일은 S3에 남아있을 수 있음): {}", uploadedFileNames, deleteException);
        }
    }

    private List<PostFile> uploadAndSaveFiles(Long postId, List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        // S3에 파일 업로드
        List<FileUploadResponse> uploadFiles = fileService.uploadFiles(files);

        // PostFile 엔티티 생성 및 저장
        List<PostFile> postFiles = IntStream.range(0, uploadFiles.size())
                .mapToObj(index -> {
                    FileUploadResponse uploadFile = uploadFiles.get(index);
                    return PostFile.of(postId, uploadFile, index+1);
                })
                .toList();

        return postService.savePostFiles(postFiles);
    }

    /**
     * 참고 링크 요청을 엔티티로 변환해 저장한다.
     *
     * @param postId 게시글 ID
     * @param linkRequests 링크 요청 목록
     * @return 저장된 링크 목록
     */
    private List<PostLink> savePostLinks(Long postId, List<PostLinkRequest> linkRequests) {
        if (linkRequests == null || linkRequests.isEmpty()) {
            return List.of();
        }
        List<PostLink> links = linkRequests.stream()
                .map(request -> PostLink.of(postId, request.referenceLink(), request.linkDescription()))
                .toList();
        return postService.savePostLinks(links);
    }
}
