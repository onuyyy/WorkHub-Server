package com.workhub.projectNode.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.api.ProjectNodeApi;
import com.workhub.projectNode.dto.*;
import com.workhub.projectNode.service.CreateProjectNodeService;
import com.workhub.projectNode.service.DeleteProjectNodeService;
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
    private final DeleteProjectNodeService deleteProjectNodeService;

    @GetMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<ConfirmStatusResponse>> getNodeStatus(@PathVariable("projectId") Long projectId,
                                                                            @PathVariable("nodeId") Long nodeId) {

        ConfirmStatusResponse nodeConfirmStatus = readProjectNodeService.getNodeConfirmStatus(projectId, nodeId);

        return ApiResponse.success(nodeConfirmStatus);
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<NodeResponse>>> getNodeList(@PathVariable("projectId") Long projectId){

        List<NodeResponse> nodeListByProject = readProjectNodeService.getNodeListByProject(projectId);

        return ApiResponse.success(nodeListByProject);

    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateNodeResponse>> createNode(@PathVariable Long projectId,
                                                                      @RequestBody CreateNodeRequest request) {

        CreateNodeResponse response = createProjectNodeService.createNode(projectId, request);
        return ApiResponse.created(response, "프로젝트 노드가 생성되었습니다.");
    }

    @Override
    @PatchMapping("/{nodeId}/status")
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

    @Override
    @PutMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<CreateNodeResponse>> updateNode(@PathVariable Long projectId,
                                                                      @PathVariable Long nodeId,
                                                                      @RequestBody UpdateNodeRequest request) {

        CreateNodeResponse response = updateProjectNodeService.updateNode(projectId, nodeId, request);

        return ApiResponse.success(response);
    }

    @Override
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<ApiResponse<String>> deleteNode(@PathVariable Long projectId,
                                                          @PathVariable Long nodeId) {

        deleteProjectNodeService.deleteProjectNode(projectId, nodeId);
        return ApiResponse.success("프로젝트 노드 삭제 완료.");
    }

    @PatchMapping("{nodeId}/confirm")
    public ResponseEntity<ApiResponse<String>> requestConfirm(@PathVariable("projectId") Long projectId,
                                                              @PathVariable("nodeId") Long nodeId,
                                                              @RequestBody ClientStatusRequest request) {

        updateProjectNodeService.updateConfirm(projectId, nodeId, request);
        return ApiResponse.success("노드 승인을 요청하였습니다.");
    }

}
