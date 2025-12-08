package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.dto.UpdateStatusRequest;
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

        recordFieldChangesIfNeeded(original, request);
        original.update(request);

        return ProjectResponse.from(original);
    }

    /**
     * 필드 변경 감지 및 히스토리 기록
     */
    private void recordFieldChangesIfNeeded(Project original, CreateProjectRequest request) {

        Long projectId = original.getProjectId();
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(original);

        // projectTitle 변경 체크
        if (request.projectName() != null &&
                !request.projectName().equals(original.getProjectTitle())) {
        }

        // projectDescription 변경 체크
        if (request.projectDescription() != null &&
                !request.projectDescription().equals(original.getProjectDescription())) {
        }

        // contractStartDate 변경 체크
        if (request.starDate() != null &&
                !request.starDate().equals(original.getContractStartDate())) {
        }

        // contractEndDate 변경 체크
        if (request.endDate() != null &&
                !request.endDate().equals(original.getContractEndDate())) {
        }

        // clientCompanyId 변경 체크
        if (request.company() != null &&
                !request.company().equals(original.getClientCompanyId())) {
        }

        historyRecorder.recordHistory(
                HistoryType.PROJECT, projectId, ActionType.UPDATE, snapshot
        );
    }

}
