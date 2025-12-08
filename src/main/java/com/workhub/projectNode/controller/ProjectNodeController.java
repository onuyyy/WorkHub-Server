package com.workhub.projectNode.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.api.ProjectNodeApi;
import com.workhub.projectNode.dto.*;
import com.workhub.projectNode.service.CreateProjectNodeService;
import com.workhub.projectNode.service.ReadProjectNodeService;
import com.workhub.projectNode.service.UpdateProjectNodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes")
@RequiredArgsConstructor
public class ProjectNodeController implements ProjectNodeApi {

    private final CreateProjectNodeService createProjectNodeService;
    private final UpdateProjectNodeService updateProjectNodeService;
    private final ReadProjectNodeService readProjectNodeService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<NodeListResponse>>> getNodeList(@PathVariable("projectId") Long projectId){

        List<NodeListResponse> nodeListByProject = readProjectNodeService.getNodeListByProject(projectId);

        return ApiResponse.success(nodeListByProject);

    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CreateNodeResponse>> createNode(@PathVariable Long projectId,
                                                                      @RequestBody CreateNodeRequest request) {

        CreateNodeResponse response = createProjectNodeService.createNode(projectId, request);
        return ApiResponse.created(response, "프로젝트 노드가 생성되었습니다.");
    }

    @Override
    @PatchMapping("{nodeId}/status")
    public ResponseEntity<ApiResponse<String>> updateNodeStatus(@PathVariable("projectId") Long projectId,
                                                                @PathVariable("nodeId") Long nodeId,
                                                                @RequestBody UpdateNodeStatusRequest request) {

        updateProjectNodeService.updateNodeStatus(projectId, nodeId, request);
        return ApiResponse.success("노드 상태 변경 성공");
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<ApiResponse<String>> updateNodeOrder(@PathVariable("projectId") Long projectId,
                                                               @RequestBody List<UpdateNodOrderRequest> request) {

        updateProjectNodeService.updateNodeOrder(projectId, request);

        return ApiResponse.success("노드 순서 변경 성공");
    }
}
