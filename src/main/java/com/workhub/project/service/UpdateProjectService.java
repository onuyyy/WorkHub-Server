package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.dto.UpdateStatusRequest;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectHistory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProjectService {

    private final ProjectService projectService;

    /**
     * 프로젝트 상태를 업데이트하고 변경 이력을 저장.
     * @param projectId 업데이트할 프로젝트 ID
     * @param statusRequest 변경할 상태 정보
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void updateProjectStatus(Long projectId,
                                    UpdateStatusRequest statusRequest,
                                    String userIp, String userAgent, Long userId) {

        Project original = projectService.findProjectById(projectId);
        String beforeStatus = original.getStatus().toString();

        original.updateProjectStatus(statusRequest.status());
        updateProjectHistory(projectId, beforeStatus, userIp, userAgent, userId);

    }

    /**
     * 프로젝트 정보를 업데이트하고 변경된 필드별로 이력을 저장.
     * 변경된 필드만 감지하여 각 필드마다 개별 히스토리 레코드를 생성.
     *
     * @param projectId 업데이트할 프로젝트 ID
     * @param request   업데이트할 프로젝트 정보
     * @param userIp    요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId    요청자 사용자 ID
     * @return
     */
    public ProjectResponse updateProject(Long projectId,
                                         CreateProjectRequest request,
                                         String userIp, String userAgent, Long userId) {

        Project original = projectService.findProjectById(projectId);
        Long originalCreator = projectService.getProjectOriginalCreator(projectId);

        List<ProjectHistory> histories = detectAndCreateHistories(original, request, originalCreator, userId, userIp, userAgent);

        histories.forEach(projectService::updateProjectHistory);
        original.update(request);

        return ProjectResponse.from(original);
    }

    /**
     * 프로젝트의 변경된 필드를 감지하고 각 필드별로 히스토리 레코드를 생성.
     * 5개 필드(제목, 설명, 시작일, 종료일, 고객사)를 비교하여 변경된 필드만 히스토리에 기록.
     * @param original 변경 전 프로젝트 엔티티
     * @param request 업데이트 요청 데이터
     * @param originalCreator 프로젝트 최초 생성자 ID
     * @param userId 현재 수정자 사용자 ID
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @return 생성된 히스토리 레코드 리스트
     */
    private List<ProjectHistory> detectAndCreateHistories(Project original,
                                                          CreateProjectRequest request, Long originalCreator,
                                                          Long userId, String userIp, String userAgent) {

        List<ProjectHistory> histories = new ArrayList<>();

        // projectTitle 변경 체크
        if (request.projectName() != null &&
            !request.projectName().equals(original.getProjectTitle())) {
            histories.add(ProjectHistory.of(
                original.getProjectId(), ActionType.UPDATE, original.getProjectTitle(),
                    originalCreator, userId, userIp, userAgent
            ));
        }

        // projectDescription 변경 체크
        if (request.projectDescription() != null &&
            !request.projectDescription().equals(original.getProjectDescription())) {
            histories.add(ProjectHistory.of(
                    original.getProjectId(), ActionType.UPDATE, original.getProjectDescription(),
                    originalCreator, userId, userIp, userAgent
            ));
        }

        // contractStartDate 변경 체크
        if (request.starDate() != null &&
            !request.starDate().equals(original.getContractStartDate())) {
            histories.add(ProjectHistory.of(
                    original.getProjectId(), ActionType.UPDATE, original.getContractStartDate().toString(),
                    originalCreator, userId, userIp, userAgent
            ));
        }

        // contractEndDate 변경 체크
        if (request.endDate() != null &&
            !request.endDate().equals(original.getContractEndDate())) {
            histories.add(ProjectHistory.of(
                    original.getProjectId(), ActionType.UPDATE, original.getContractEndDate().toString(),
                    originalCreator, userId, userIp, userAgent
            ));
        }

        // clientCompanyId 변경 체크
        if (request.company() != null &&
            !request.company().equals(original.getClientCompanyId())) {
            histories.add(ProjectHistory.of(
                    original.getProjectId(), ActionType.UPDATE, original.getClientCompanyId().toString(),
                    originalCreator, userId, userIp, userAgent
            ));
        }

        return histories;
    }

    /**
     * 프로젝트 상태 변경 이력을 저장.
     * @param projectId 프로젝트 ID
     * @param beforeStatus 변경 전 상태
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    private void updateProjectHistory(Long projectId, String beforeStatus,
                                      String userIp, String userAgent, Long userId) {

        Long originalCreator = projectService.getProjectOriginalCreator(projectId);

        projectService.updateProjectHistory(ProjectHistory.of(projectId,
                ActionType.UPDATE, beforeStatus, originalCreator,
                userId, userIp, userAgent));
    }
}
