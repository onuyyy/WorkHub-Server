package com.workhub.checklist.controller;

import com.workhub.checklist.api.CheckListCommentApi;
import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.dto.comment.CheckListCommentUpdateRequest;
import com.workhub.checklist.service.comment.CreateCheckListCommentService;
import com.workhub.checklist.service.comment.DeleteCheckListCommentService;
import com.workhub.checklist.service.comment.ReadCheckListCommentService;
import com.workhub.checklist.service.comment.UpdateCheckListCommentService;
import com.workhub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/checkLists")
public class CheckListCommentController implements CheckListCommentApi {

    private final CreateCheckListCommentService createCheckListCommentService;
    private final ReadCheckListCommentService readCheckListCommentService;
    private final UpdateCheckListCommentService updateCheckListCommentService;
    private final DeleteCheckListCommentService deleteCheckListCommentService;

    @Override
    @PostMapping(value = "/{checkListId}/items/{checkListItemId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CheckListCommentResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @Valid @RequestPart("data") CheckListCommentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {

        CheckListCommentResponse response =
                createCheckListCommentService.create(projectId, nodeId, checkListId, checkListItemId, request, files);

        return ApiResponse.created(response, "체크리스트 댓글이 작성되었습니다.");
    }

    @Override
    @PatchMapping(value = "/{checkListId}/items/{checkListItemId}/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CheckListCommentResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @PathVariable Long commentId,
            @Valid @RequestPart("data") CheckListCommentUpdateRequest request,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles
    ) {
        CheckListCommentResponse response = updateCheckListCommentService.update(
                projectId, nodeId, checkListId, checkListItemId, commentId, request, newFiles);

        return ApiResponse.success(response, "체크리스트 댓글이 수정되었습니다.");
    }

    @Override
    @GetMapping(value = "/{checkListId}/items/{checkListItemId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<CheckListCommentResponse>>> findComments(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId
    ) {
        List<CheckListCommentResponse> response = readCheckListCommentService.findComments(
                projectId, nodeId, checkListId, checkListItemId);

        return ApiResponse.success(response, "체크리스트 댓글이 조회되었습니다.");
    }

    @Override
    @DeleteMapping(value = "/{checkListId}/items/{checkListItemId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @PathVariable Long commentId
    ) {
        Long deletedId = deleteCheckListCommentService.delete(
                projectId, nodeId, checkListId, checkListItemId, commentId);

        return ApiResponse.success(deletedId, "체크리스트 댓글이 삭제되었습니다.");
    }

}
