package com.workhub.project.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.project.api.ProjectApi;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectListResponse;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.dto.UpdateStatusRequest;
import com.workhub.project.service.CreateProjectService;
import com.workhub.project.service.DeleteProjectService;
import com.workhub.project.service.ReadProjectService;
import com.workhub.project.service.UpdateProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Slf4j
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {

    private final CreateProjectService createProjectService;
    private final UpdateProjectService updateProjectService;
    private final DeleteProjectService deleteProjectService;
    private final ReadProjectService readProjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest projectRequest) {

        ProjectResponse projectResponse = createProjectService.createProject(projectRequest);
        return ApiResponse.created(projectResponse, "프로젝트가 생성되었습니다.");
    }

    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateStatus(@PathVariable("projectId") Long projectId,
                                                            @RequestBody UpdateStatusRequest request) {

        updateProjectService.updateProjectStatus(projectId, request);

        return ApiResponse.success("상태 변경 성공");
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable("projectId") Long projectId,
                                                                      @RequestBody CreateProjectRequest request) {

        ProjectResponse projectResponse = updateProjectService.updateProject(projectId, request);
        return ApiResponse.success(projectResponse, "프로젝트 수정에 성공했습니다.");
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProject(@PathVariable("projectId") Long projectId) {

        deleteProjectService.deleteProject(projectId);
        return ApiResponse.success("프로젝트가 삭제되었습니다.");
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ProjectListResponse>>> projectList() {

        List<ProjectListResponse> responses = readProjectService.projectList();
        return ApiResponse.success(responses);
    }
}
