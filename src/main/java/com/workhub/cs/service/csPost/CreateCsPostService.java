package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.global.entity.ActionType;
import com.workhub.project.service.ProjectService;
import com.workhub.cs.service.CsPostNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;
    private final CsPostNotificationService csPostNotificationService;

    /**
     * CS POST를 작성한다.
     * @param projectId 프로젝트 식별자
     * @param userId 유저 식별자
     * @param request
     * @return CsPostResponse
     */
    public CsPostResponse create(Long projectId, Long userId, CsPostRequest request) {

        projectService.validateCompletedProject(projectId);

        CsPost csPost = csPostService.save(CsPost.of(projectId, userId, request));

        List<CsPostFile> files = List.of();

        if (request.files() != null && !request.files().isEmpty()) {

            files = request.files().stream()
                    .map(f -> CsPostFile.of(csPost.getCsPostId(), f))
                    .toList();

            csPostService.saveAllFiles(files);
        }

        csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.CREATE);
        csPostNotificationService.notifyCsPostCreated(projectId, csPost.getCsPostId(), csPost.getTitle());

        return CsPostResponse.from(csPost, files);
    }

}
