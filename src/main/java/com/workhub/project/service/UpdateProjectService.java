package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.dto.request.UpdateStatusRequest;
import com.workhub.project.entity.Project;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProjectService {

    private final ProjectService projectService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트 상태를 업데이트하고 변경 이력을 저장.
     * @param projectId 업데이트할 프로젝트 ID
     * @param statusRequest 변경할 상태 정보
     */
    public void updateProjectStatus(Long projectId,
                                    UpdateStatusRequest statusRequest) {

        Project original = projectService.findProjectById(projectId);
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(original);
        //String beforeStatus = original.getStatus().toString();

        original.updateProjectStatus(statusRequest.status());
        historyRecorder.recordHistory(HistoryType.PROJECT, projectId, ActionType.UPDATE, snapshot);

    }

    /**
     * 프로젝트 정보를 업데이트하고 변경된 필드별로 이력을 저장.
     * 변경된 필드만 감지하여 각 필드마다 개별 히스토리 레코드를 생성.
     *
     * @param projectId 업데이트할 프로젝트 ID
     * @param request   업데이트할 프로젝트 정보
     * @return 변경된 엔티티 응답
     */
    public ProjectResponse updateProject(Long projectId, CreateProjectRequest request) {

        Project original = projectService.findProjectById(projectId);
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(original);

        original.update(request);
        historyRecorder.recordHistory(HistoryType.PROJECT, projectId, ActionType.UPDATE, snapshot);

        return ProjectResponse.from(original);
    }

}
