package com.workhub.checklist.controller;

import com.workhub.checklist.api.CheckListApi;
import com.workhub.checklist.dto.CheckListCreateRequest;
import com.workhub.checklist.dto.CheckListItemStatus;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/checkLists")
public class CheckListController implements CheckListApi {

    private final CreateCheckListService createCheckListService;
    private final ReadCheckListService readCheckListService;
    private final UpdateCheckListService updateCheckListService;

    @Override
    @PostMapping
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
    @GetMapping
    public ResponseEntity<ApiResponse<CheckListResponse>> findCheckList(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    ) {

        CheckListResponse response = readCheckListService.findCheckList(projectId, nodeId);

        return ApiResponse.success(response, "체크리스트가 조회되었습니다.");
    }

    @Override
    @PatchMapping
    public ResponseEntity<ApiResponse<CheckListResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody CheckListUpdateRequest request
    ) {

        CheckListResponse response = updateCheckListService.update(projectId, nodeId, request);

        return ApiResponse.success(response, "체크리스트가 수정되었습니다.");
    }

    @Override
    @PatchMapping("/{checkListId}/items/{checkListItemId}/status")
    public ResponseEntity<ApiResponse<CheckListItemStatus>> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @RequestParam CheckListItemStatus status
    ) {

        CheckListItemStatus response =
                updateCheckListService.updateStatus(projectId, nodeId, checkListId, checkListItemId, status);

        return ApiResponse.success(response, "체크리스트 아이템 상태가 입력되었습니다.");
    }

}
