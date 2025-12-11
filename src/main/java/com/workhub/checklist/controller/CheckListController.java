package com.workhub.checklist.controller;

import com.workhub.checklist.api.CheckListApi;
import com.workhub.checklist.dto.CheckListCreateRequest;
import com.workhub.checklist.dto.CheckListResponse;
import com.workhub.checklist.dto.CheckListUpdateRequest;
import com.workhub.checklist.service.CreateCheckListService;
import com.workhub.checklist.service.ReadCheckListService;
import com.workhub.checklist.service.UpdateCheckListService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}")
public class CheckListController implements CheckListApi {

    private final CreateCheckListService createCheckListService;
    private final ReadCheckListService readCheckListService;
    private final UpdateCheckListService updateCheckListService;

    @Override
    @PostMapping("/checkLists")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CheckListResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody CheckListCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        CheckListResponse response =
                createCheckListService.create(projectId, nodeId, userDetails.getUserId(), request);

        return ApiResponse.created(response, "체크리스트가 생성되었습니다.");
    }

    @Override
    @GetMapping("/checkLists")
    public ResponseEntity<ApiResponse<CheckListResponse>> findCheckList(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    ) {

        CheckListResponse response = readCheckListService.findCheckList(projectId, nodeId);

        return ApiResponse.success(response, "체크리스트가 조회되었습니다.");
    }

    @Override
    @PatchMapping("/checkLists")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CheckListResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody CheckListUpdateRequest request
    ) {

        CheckListResponse response = updateCheckListService.update(projectId, nodeId, request);

        return ApiResponse.success(response, "체크리스트가 수정되었습니다.");
    }

}
