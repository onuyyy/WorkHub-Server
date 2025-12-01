package com.workhub.project.controller;

import com.workhub.global.clientInfo.ClientInfo;
import com.workhub.global.clientInfo.ClientInfoDto;
import com.workhub.global.response.ApiResponse;
import com.workhub.project.api.ProjectApi;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.service.CreateProjectService;
import com.workhub.userTable.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Slf4j
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {

    private final CreateProjectService createProjectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest projectRequest,
                                                                      @Parameter(hidden = true) @ClientInfo ClientInfoDto clientInfoDto,
                                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();

        log.info("userId : {}, requestIp : {}", userId, clientInfoDto.getIpAddress());
        ProjectResponse projectResponse = createProjectService.createProject(projectRequest, userId, clientInfoDto.getIpAddress(), clientInfoDto.getUserAgent());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(projectResponse, "프로젝트가 생성되었습니다."));
    }

}
