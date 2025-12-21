package com.workhub.checklist.controller;

import com.workhub.checklist.api.CheckListApi;
import com.workhub.checklist.dto.checkList.CheckListCreateRequest;
import com.workhub.checklist.dto.checkList.CheckListItemStatus;
import com.workhub.checklist.dto.checkList.CheckListResponse;
import com.workhub.checklist.dto.checkList.CheckListTemplateRequest;
import com.workhub.checklist.dto.checkList.CheckListTemplateResponse;
import com.workhub.checklist.dto.checkList.CheckListUpdateRequest;
import com.workhub.checklist.service.checkList.CreateCheckListService;
import com.workhub.checklist.service.checkList.CreateCheckListTemplateService;
import com.workhub.checklist.service.checkList.ReadCheckListService;
import com.workhub.checklist.service.checkList.ReadCheckListTemplateService;
import com.workhub.checklist.service.checkList.UpdateCheckListService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/checkLists")
public class CheckListController implements CheckListApi {

    private final CreateCheckListService createCheckListService;
    private final ReadCheckListService readCheckListService;
    private final UpdateCheckListService updateCheckListService;
    private final CreateCheckListTemplateService createCheckListTemplateService;
    private final ReadCheckListTemplateService readCheckListTemplateService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CheckListResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestPart("data") CheckListCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        CheckListResponse response =
                createCheckListService.create(projectId, nodeId, userDetails.getUserId(), request, files);

        return ApiResponse.created(response, "체크리스트가 생성되었습니다.");
    }

    @Override
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<CheckListTemplateResponse>>> findTemplates(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    ) {

        List<CheckListTemplateResponse> response =
                readCheckListTemplateService.findAll(projectId, nodeId);

        return ApiResponse.success(response, "체크리스트 템플릿 목록이 조회되었습니다.");
    }

    @Override
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<ApiResponse<CheckListTemplateResponse>> findTemplateById(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long templateId
    ) {

        CheckListTemplateResponse response =
                readCheckListTemplateService.findById(projectId, nodeId, templateId);

        return ApiResponse.success(response, "체크리스트 템플릿이 조회되었습니다.");
    }

    @Override
    @PostMapping(value = "/templates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CheckListTemplateResponse>> createTemplate(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody CheckListTemplateRequest request
    ) {

        CheckListTemplateResponse response =
                createCheckListTemplateService.create(projectId, nodeId, request);

        return ApiResponse.created(response, "체크리스트 템플릿이 저장되었습니다.");
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
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CheckListResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestPart("data") CheckListUpdateRequest request,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles
    ) {

        CheckListResponse response = updateCheckListService.update(projectId, nodeId, request, newFiles);

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
