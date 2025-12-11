package com.workhub.project.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.project.api.ProjectApi;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.request.UpdateStatusRequest;
import com.workhub.project.dto.response.PagedProjectListResponse;
import com.workhub.project.dto.request.ProjectListRequest;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.entity.Status;
import com.workhub.project.service.CreateProjectService;
import com.workhub.project.service.DeleteProjectService;
import com.workhub.project.service.ReadProjectService;
import com.workhub.project.service.UpdateProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    /**
     * 페이징, 필터링, 정렬이 적용된 프로젝트 목록 조회 (무한 스크롤용)
     *
     * @param startDate 계약 시작일 검색 범위 시작 (Optional, 기본값: 1년 전)
     * @param endDate 계약 시작일 검색 범위 종료 (Optional, 기본값: 현재 날짜)
     * @param status 프로젝트 상태 (Optional, 기본값: 전체)
     * @param sortOrder 정렬 조건 (Optional, 기본값: LATEST)
     * @param cursor 커서 (마지막 조회한 projectId)
     * @param size 페이지 크기 (Optional, 기본값: 9, 최대: 100)
     * @return 페이징된 프로젝트 목록
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PagedProjectListResponse>> getProjects(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) ProjectListRequest.SortOrder sortOrder,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size
    ) {

        PagedProjectListResponse response = readProjectService.projectListWithPaging(startDate, endDate, status,
                sortOrder, cursor, size);
        return ApiResponse.success(response);
    }

}