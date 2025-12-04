package com.workhub.projectNode.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.api.ProjectNodeApi;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.service.CreateProjectNodeService;
import com.workhub.projectNode.service.UpdateProjectNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes")
@RequiredArgsConstructor
public class ProjectNodeController implements ProjectNodeApi {

    private final CreateProjectNodeService createProjectNodeService;
    private final UpdateProjectNodeService updateProjectNodeService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CreateNodeResponse>> createNode(@PathVariable Long projectId,
                                                                      @RequestBody CreateNodeRequest request) {

        CreateNodeResponse response = createProjectNodeService.createNode(projectId, request);
        return ApiResponse.created(response, "프로젝트 노드가 생성되었습니다.");
    }

    @Override
    @PatchMapping("{nodeId}/status")
    public ResponseEntity<ApiResponse<String>> updateNodeStatus(@PathVariable("nodeId") Long nodeId,
                                                                @RequestBody UpdateNodeStatusRequest request) {

        updateProjectNodeService.updateNodeStatus(nodeId, request);
        return ApiResponse.success("노드 상태 변경 성공");
    }
}
