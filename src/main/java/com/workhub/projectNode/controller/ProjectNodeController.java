package com.workhub.projectNode.controller;

import com.workhub.global.clientInfo.ClientInfo;
import com.workhub.global.clientInfo.ClientInfoDto;
import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.api.ProjectNodeApi;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.service.CreateProjectNodeService;
import com.workhub.userTable.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes")
@RequiredArgsConstructor
public class ProjectNodeController implements ProjectNodeApi {

    private final CreateProjectNodeService createProjectNodeService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateNodeResponse>> createNode(@PathVariable Long projectId,
                                                                      @RequestBody CreateNodeRequest request,
                                                                      @Parameter(hidden = true) @ClientInfo ClientInfoDto clientInfoDto,
                                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        CreateNodeResponse response = createProjectNodeService.createNode(projectId, request, userDetails.getUserId(),
                clientInfoDto.getIpAddress(), clientInfoDto.getUserAgent());

        return ApiResponse.created(response, "프로젝트 노드가 생성되었습니다.");
    }
}
